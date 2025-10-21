package org.stackspotapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cdimascio.dotenv.Dotenv;
import org.stackspotapi.dto.ChatRequestDto;
import org.stackspotapi.dto.CreateExecutionRequestDto;
import org.stackspotapi.dto.CreateExecutionResponseDto;
import org.stackspotapi.dto.EnsureDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

@Service
public class AiChatService {

    private static final String CHAT_API_URL = "https://genai-code-buddy-api.stackspot.com/v3/chat";
    private static final HttpClient httpClient = HttpClient.newHttpClient( );
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Dotenv dotenv = Dotenv.load();
    private static final String AGENT_ID = dotenv.get("AGENT_ID");

    // O estado do token agora é gerenciado de forma estática para ser reutilizado entre as chamadas.
    private static EnsureDto tokenState = null;

    // Gerenciamento do estado da conversação para reutilização.
    private static String conversationIdState = null;
    private static int conversationRequestCount = 0;
    private static final int MAX_REQUESTS_PER_CONVERSATION = 10;

    /**
     * Orquestra o fluxo completo para enviar um prompt à IA e obter uma resposta.
     *
     * @param prompt O prompt do usuário.
     * @return A resposta da IA como uma String JSON `{"answer": "..."}` ou nulo em caso de falha.
     */
    public static String ask(String prompt) {
        System.out.println("--- Passo 1: Garantindo token de autenticação válido ---");
        tokenState = EnsureTokenService.ensureValidToken(tokenState);

        if (tokenState == null) {
            System.err.println("Falha na autenticação. Encerrando.");
            return createErrorJson("Falha na autenticação. Não foi possível obter o token.");
        }

        // --- Passo 2 e 3: Gerenciamento do ID da Conversação ---
        // Verifica se precisa de um novo conversation_id (se não existir ou se o limite foi atingido)
        if (conversationIdState == null || conversationRequestCount >= MAX_REQUESTS_PER_CONVERSATION) {
            System.out.println("\n--- Iniciando uma nova conversação ---");
            if (conversationIdState != null) {
                System.out.println("Limite de " + MAX_REQUESTS_PER_CONVERSATION + " requisições atingido para a conversação anterior.");
            }

            // Cria uma nova execução para obter um novo conversation_id
            CreateExecutionRequestDto execRequest = new CreateExecutionRequestDto("query.java", prompt);
            CreateExecutionResponseDto execResponse = ExecutionService.createExecution(execRequest, tokenState);
            if (execResponse == null) {
                System.err.println("Falha ao criar a execução. Encerrando.");
                return createErrorJson("Falha ao criar a execução na plataforma.");
            }
            String executionId = execResponse.getExecutionId();

            // Obtém o ID da conversação da nova execução
            try {
                System.out.println("Aguardando 5 segundos para a IA processar...");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrompida durante a espera.");
                return createErrorJson("Processo interrompido durante a espera pela IA.");
            }
            conversationIdState = ExecutionService.getConversationId(executionId, tokenState);
            if (conversationIdState == null) {
                System.err.println("Falha ao obter o ID da conversação. Encerrando.");
                return createErrorJson("Falha ao obter o ID da conversação após a execução.");
            }

            System.out.println("Nova conversação criada com ID: " + conversationIdState);
            conversationRequestCount = 0; // Reseta o contador para a nova conversação
        } else {
            System.out.println("\n--- Reutilizando conversação existente ID: " + conversationIdState + " ---");
        }

        // Incrementa o contador de requisições para a conversação atual
        conversationRequestCount++;
        System.out.println("Requisição " + conversationRequestCount + "/" + MAX_REQUESTS_PER_CONVERSATION + " para esta conversação.");

        System.out.println("\n--- Passo 4: Enviando prompt para o chat da IA ---");
        return sendPromptToChat(prompt, conversationIdState, tokenState);
    }

    /**
     * Envia o prompt para o endpoint de chat e retorna a resposta formatada como uma String JSON.
     */
    private static String sendPromptToChat(String userPrompt, String conversationId, EnsureDto tokenDto) {
        try {
            ChatRequestDto.Context context = new ChatRequestDto.Context(conversationId, AGENT_ID);
            ChatRequestDto chatRequest = new ChatRequestDto(context, userPrompt);
            String payloadJson = objectMapper.writeValueAsString(chatRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CHAT_API_URL))
                    .header("Authorization", "Bearer " + tokenDto.getJwt())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
                    .build();

            HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines( ));

            if (response.statusCode() != 200) {
                System.err.printf("StackSpot IA retornou erro: %d%n", response.statusCode());
                return createErrorJson("API da IA retornou um erro com status: " + response.statusCode());
            }

            StringBuilder answerBuilder = new StringBuilder();
            response.body().forEach(line -> {
                if (line.startsWith("data: ")) {
                    String jsonData = line.substring(6);
                    if (!jsonData.trim().isEmpty()) {
                        try {
                            JsonNode parsed = objectMapper.readTree(jsonData);
                            if (parsed.has("answer")) {
                                answerBuilder.append(parsed.get("answer").asText());
                            }
                        } catch (JsonProcessingException e) {
                            // Ignora linhas JSON malformadas
                        }
                    }
                }
            });

            String finalAnswer = answerBuilder.toString().strip();

            ObjectNode jsonAnswerNode = objectMapper.createObjectNode();
            jsonAnswerNode.put("answer", finalAnswer);

            System.out.println("Resposta da IA recebida com sucesso.");
            return objectMapper.writeValueAsString(jsonAnswerNode);

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro na comunicação com a StackSpot IA: " + e.getMessage());
            Thread.currentThread().interrupt();
            return createErrorJson("Erro na comunicação com a IA: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar para criar uma String JSON de erro padronizada.
     */
    private static String createErrorJson(String message) {
        try {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("answer", "Erro: " + message);
            return objectMapper.writeValueAsString(errorNode);
        } catch (JsonProcessingException jsonEx) {
            return "{\"answer\":\"Erro crítico ao gerar JSON de erro.\"}"; // Fallback
        }
    }
}
