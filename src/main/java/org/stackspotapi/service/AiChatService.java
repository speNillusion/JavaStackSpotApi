// No arquivo service/AiChatService.java
package org.stackspotapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode; // Importação necessária
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

    // Removi a classe interna JsonAnswer, pois usaremos o ObjectMapper da Jackson.

    private static final String CHAT_API_URL = "https://genai-code-buddy-api.stackspot.com/v3/chat";
    private static final HttpClient httpClient = HttpClient.newHttpClient( );
    private static final ObjectMapper objectMapper = new ObjectMapper(); // Já temos o ObjectMapper, vamos usá-lo!
    private static final Dotenv dotenv = Dotenv.load();
    private static final String AGENT_ID = dotenv.get("AGENT_ID");

    /**
     * Orquestra o fluxo completo para enviar um prompt à IA e obter uma resposta.
     *
     * @param prompt O prompt do usuário.
     * @return A resposta da IA como uma String JSON `{"answer": "..."}` ou nulo em caso de falha.
     */
    public static String ask(String prompt) {
        // ... (o método ask está correto, nenhuma mudança necessária aqui)
        System.out.println("--- Passo 1: Obtendo token de autenticação ---");
        EnsureDto tokenState = EnsureTokenService.ensureValidToken(null);
        if (tokenState == null) {
            System.err.println("Falha na autenticação. Encerrando.");
            return null;
        }

        System.out.println("\n--- Passo 2: Criando uma execução ---");
        CreateExecutionRequestDto execRequest = new CreateExecutionRequestDto("query.java", prompt);
        CreateExecutionResponseDto execResponse = ExecutionService.createExecution(execRequest, tokenState);
        if (execResponse == null) {
            System.err.println("Falha ao criar a execução. Encerrando.");
            return null;
        }
        String executionId = execResponse.getExecutionId();

        System.out.println("\n--- Passo 3: Obtendo o ID da conversação ---");
        try {
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

        System.out.println("\n--- Passo 4: Enviando prompt para o chat da IA ---");
        return sendPromptToChat(prompt, conversationId, tokenState);
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
                return null;
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
                            // Ignora linhas JSON malformadas, continuando o processamento
                        }
                    }
                }
            });

            String finalAnswer = answerBuilder.toString().strip();

            // Cria um nó de objeto JSON usando o ObjectMapper que já existe
            ObjectNode jsonAnswerNode = objectMapper.createObjectNode();
            jsonAnswerNode.put("answer", finalAnswer);

            System.out.println("Resposta da IA recebida com sucesso.");

            // Converte o nó do objeto para uma String JSON e a retorna
            return objectMapper.writeValueAsString(jsonAnswerNode);

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro na comunicação com a StackSpot IA: " + e.getMessage());
            Thread.currentThread().interrupt();
            // Em caso de exceção, também é bom retornar um JSON de erro
            try {
                ObjectNode errorNode = objectMapper.createObjectNode();
                errorNode.put("answer", "Erro: Falha na comunicação com a IA. " + e.getMessage());
                return objectMapper.writeValueAsString(errorNode);
            } catch (JsonProcessingException jsonEx) {
                return "{\"answer\":\"Erro crítico ao gerar JSON de erro.\"}"; // Fallback final
            }
        }
    }
}
