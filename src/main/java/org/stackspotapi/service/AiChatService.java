// No arquivo service/AiChatService.java
package org.stackspotapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.stackspotapi.dto.ChatRequestDto;
import org.stackspotapi.dto.CreateExecutionRequestDto;
import org.stackspotapi.dto.CreateExecutionResponseDto;
import org.stackspotapi.dto.EnsureDto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

public class AiChatService {

    private static final String CHAT_API_URL = "https://genai-code-buddy-api.stackspot.com/v3/chat";
    private static final HttpClient httpClient = HttpClient.newHttpClient( );
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Dotenv dotenv = Dotenv.load();
    private static final String AGENT_ID = dotenv.get("AGENT_ID");

    /**
     * Orquestra o fluxo completo para enviar um prompt à IA e obter uma resposta.
     *
     * @param prompt O prompt do usuário.
     * @return A resposta da IA como uma String, ou nulo em caso de falha.
     */
    public static String ask(String prompt) {
        // --- PASSO 1: AUTENTICAR ---
        System.out.println("--- Passo 1: Obtendo token de autenticação ---");
        EnsureDto tokenState = EnsureTokenService.ensureValidToken(null);
        if (tokenState == null) {
            System.err.println("Falha na autenticação. Encerrando.");
            return null;
        }

        // --- PASSO 2: CRIAR EXECUÇÃO ---
        System.out.println("\n--- Passo 2: Criando uma execução ---");
        CreateExecutionRequestDto execRequest = new CreateExecutionRequestDto("query.java", prompt);
        CreateExecutionResponseDto execResponse = ExecutionService.createExecution(execRequest, tokenState);
        if (execResponse == null) {
            System.err.println("Falha ao criar a execução. Encerrando.");
            return null;
        }
        String executionId = execResponse.getExecutionId();

        // --- PASSO 3: OBTER O CONVERSATION ID ---
        System.out.println("\n--- Passo 3: Obtendo o ID da conversação ---");
        try {
            // A API pode precisar de tempo para processar.
            System.out.println("Aguardando 5 segundos para a IA processar...");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrompida durante a espera.");
            return null;
        }
        String conversationId = ExecutionService.getConversationId(executionId, tokenState);
        if (conversationId == null) {
            System.err.println("Falha ao obter o ID da conversação. Encerrando.");
            return null;
        }

        // --- PASSO 4: ENVIAR O PROMPT PARA O CHAT ---
        System.out.println("\n--- Passo 4: Enviando prompt para o chat da IA ---");
        return sendPromptToChat(prompt, conversationId, tokenState);
    }

    /**
     * Envia o prompt para o endpoint de chat e processa a resposta em streaming.
     */
    private static String sendPromptToChat(String userPrompt, String conversationId, EnsureDto tokenDto) {
        try {
            // Monta o payload complexo usando o DTO
            ChatRequestDto.Context context = new ChatRequestDto.Context(conversationId, AGENT_ID);
            ChatRequestDto chatRequest = new ChatRequestDto(context, userPrompt);
            String payloadJson = objectMapper.writeValueAsString(chatRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CHAT_API_URL))
                    .header("Authorization", "Bearer " + tokenDto.getJwt())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
                    .build();

            // Envia a requisição e obtém a resposta como um Stream de linhas
            HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines( ));

            if (response.statusCode() != 200) {
                System.err.printf("StackSpot IA retornou erro: %d%n", response.statusCode());
                return null;
            }

            // Processa a resposta em streaming (Server-Sent Events)
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
                            // Ignora linhas malformadas, como no código Python
                        }
                    }
                }
            });

            String finalAnswer = answerBuilder.toString().strip();
            if (finalAnswer.length() < 10) {
                System.err.println("Resposta da IA muito curta ou vazia.");
                return finalAnswer;
            }

            System.out.println("Resposta da IA recebida com sucesso.");
            return finalAnswer; // Aqui você pode adicionar a função de limpeza 'clean_ai_response' se necessário

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro na comunicação com a StackSpot IA: " + e.getMessage());
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
