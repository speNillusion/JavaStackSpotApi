// No arquivo service/ExecutionService.java
package org.stackspotapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cdimascio.dotenv.Dotenv;
import org.stackspotapi.dto.CreateExecutionRequestDto;
import org.stackspotapi.dto.CreateExecutionResponseDto;
import org.stackspotapi.dto.EnsureDto;
import org.stackspotapi.dto.GetConversationResponseDto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExecutionService {
    private static final Dotenv dotenv = Dotenv.load( );
    private static final String QUICK_COMMAND_NAME = dotenv.get("QUICK_COMMAND_NAME");
    private static final String API_URL = "https://genai-code-buddy-api.stackspot.com/v1/quick-commands/create-execution/" + QUICK_COMMAND_NAME;
    private static final HttpClient httpClient = HttpClient.newHttpClient( );
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Obtém o ID da conversação a partir de uma execução da StackSpot AI.
     *
     * @param executionId O ID da execução a ser consultada.
     * @param tokenDto    O DTO contendo o token de autenticação.
     * @return A String do 'conversation_id' em caso de sucesso, ou nulo em caso de falha.
     */
    public static String getConversationId(String executionId, EnsureDto tokenDto) {
        if (tokenDto == null || tokenDto.getJwt() == null || tokenDto.getJwt().isBlank()) {
            System.err.println("Token JWT não disponível. A autenticação é necessária.");
            return null;
        }
        if (executionId == null || executionId.isBlank()) {
            System.err.println("Execution ID não pode ser nulo ou vazio.");
            return null;
        }

        System.out.println("Buscando 'conversation_id' para a execução ID: " + executionId);

        try {
            // Monta a URL do endpoint de callback
            String url = "https://genai-code-buddy-api.stackspot.com/v1/quick-commands/callback/" + executionId;

            HttpRequest request = HttpRequest.newBuilder( )
                    .uri(URI.create(url))
                    .header("execution_id", executionId)
                    .header("Authorization", "Bearer " + tokenDto.getJwt())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString( ));

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                // Parseia a resposta JSON para extrair apenas o campo 'conversation_id'
                JsonNode responseBody = objectMapper.readTree(response.body());
                if (responseBody.has("conversation_id")) {
                    String conversationId = responseBody.get("conversation_id").asText();
                    System.out.println("Conversation ID obtido com sucesso: " + conversationId);
                    return conversationId;
                } else {
                    System.err.println("Resposta da API não contém o campo 'conversation_id'.");
                    return null;
                }
            } else {
                System.err.printf("Erro ao obter 'conversation_id'. Status: %d, Resposta: %s%n", response.statusCode(), response.body());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro de comunicação ao obter 'conversation_id': " + e.getMessage());
            return null;
        }
    }


    /**
     * Envia um prompt para a StackSpot AI para criar uma execução.
     *
     * @param requestDto O DTO contendo os dados da requisição (slug, prompt, etc.).
     * @param tokenDto   O DTO contendo o token de autenticação atual.
     * @return Um DTO com o ID da execução em caso de sucesso, ou nulo em caso de falha.
     */
    public static CreateExecutionResponseDto createExecution(CreateExecutionRequestDto requestDto, EnsureDto tokenDto) {
        // 1. Garante que o token de autenticação é válido, renovando se necessário.
        // A responsabilidade é do chamador, mas poderíamos chamar aqui também.
        if (tokenDto == null || tokenDto.getJwt() == null || tokenDto.getJwt().isBlank()) {
            System.err.println("Token JWT não disponível. A autenticação é necessária antes de chamar este método.");
            return null;
        }

        try {
            // 2. Monta os cabeçalhos (headers) da requisição
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("slug", requestDto.getSlug())
                    .header("Authorization", "Bearer " + tokenDto.getJwt())
                    .header("Content-Type", "application/json");

            // Adiciona o conversation_id se ele existir no DTO
            if (requestDto.getConversationId() != null && !requestDto.getConversationId().isBlank()) {
                requestBuilder.header("conversation_id", requestDto.getConversationId());
            }

            // 3. Monta o corpo (payload) da requisição
            ObjectNode payload = objectMapper.createObjectNode();
            Object prompt = requestDto.getPrompt();

            // Lógica para tratar prompt como String ou como um objeto (Map/DTO)
            if (prompt instanceof String) {
                payload.putObject("input_data").put("text", (String) prompt);
            } else {
                // Se for um objeto (Map, DTO, etc.), o Jackson vai serializá-lo
                payload.set("input_data", objectMapper.valueToTree(prompt));
            }
            String payloadJson = objectMapper.writeValueAsString(payload);

            // 4. Constrói e envia a requisição POST
            HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(payloadJson)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString( ));

            // 5. Processa a resposta
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                // A API retorna o ID da execução como uma string simples com aspas.
                String executionId = response.body().replace("\"", "");
                System.out.println("Execução criada com sucesso. ID: " + executionId);
                return new CreateExecutionResponseDto(executionId);
            } else {
                System.err.printf("Erro ao criar execução. Status: %d, Resposta: %s%n", response.statusCode(), response.body());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro de comunicação ao criar execução: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Gerenciamento do estado do token
        EnsureDto tokenState = null;

        // --- PASSO 1: AUTENTICAR ---
        tokenState = EnsureTokenService.ensureValidToken(tokenState);
        if (tokenState == null) {
            System.err.println("Não foi possível obter o token. Encerrando teste.");
            return;
        }
        System.out.println("Token obtido com sucesso.");

        // --- PASSO 2: CRIAR EXECUÇÃO ---
        System.out.println("\n--- Passo 2: Criando uma execução ---");
        CreateExecutionRequestDto executionRequest = new CreateExecutionRequestDto(
                "query.java",
                "Crie uma classe Java para representar um Produto com id, nome e preço."
        );
        CreateExecutionResponseDto executionResponse = createExecution(executionRequest, tokenState);
        if (executionResponse == null) {
            System.err.println("Falha ao criar a execução. Encerrando teste.");
            return;
        }
        String executionId = executionResponse.getExecutionId();
        System.out.println("Execução criada. ID: " + executionId);

        String conversationResponse = getConversationId(executionId, tokenState);

        System.out.println(conversationResponse);
    }

}
