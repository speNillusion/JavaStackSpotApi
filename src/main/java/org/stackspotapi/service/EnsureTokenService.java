package org.stackspotapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.stackspotapi.dto.CreateExecutionRequestDto;
import org.stackspotapi.dto.CreateExecutionResponseDto;
import org.stackspotapi.dto.EnsureDto;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

import static org.stackspotapi.service.ExecutionService.createExecution;
import static org.stackspotapi.service.ExecutionService.getConversationId;

public class EnsureTokenService {
    // Configurações carregadas do .env permanecem estáticas e finais
    private static final Dotenv dotenv = Dotenv.load( );
    private static final String REALM = dotenv.get("STACKSPOT_REALM");
    private static final String CLIENT_ID = dotenv.get("STACKSPOT_CLIENT_ID");
    private static final String CLIENT_SECRET = dotenv.get("STACKSPOT_CLIENT_SECRET");

    // Utilitários reutilizáveis
    private static final HttpClient httpClient = HttpClient.newHttpClient( );
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Garante que o DTO de token seja válido, renovando se necessário.
     *
     * @param currentTokenDto O DTO contendo o token atual. Pode ser nulo.
     * @return Um DTO com um token válido e atualizado. Retorna nulo se a autenticação falhar.
     */
    public static EnsureDto ensureValidToken(EnsureDto currentTokenDto) { // CORREÇÃO 1: O parâmetro agora é do tipo EnsureDto
        // 1. Verifica se as credenciais essenciais estão configuradas
        if (REALM == null || REALM.isBlank() || CLIENT_ID == null || CLIENT_ID.isBlank() || CLIENT_SECRET == null || CLIENT_SECRET.isBlank()) {
            System.err.println("Credenciais da StackSpot (REALM, CLIENT_ID, CLIENT_SECRET) não configuradas no .env!");
            return null;
        }

        // 2. Verifica se o token no DTO atual ainda é válido
        // CORREÇÃO 2: Usa o objeto 'currentTokenDto' para a verificação e remove o 'f' antes do if.
        if (currentTokenDto != null && currentTokenDto.getJwt() != null && !currentTokenDto.getJwt().isBlank() && currentTokenDto.getTokenExpiry() != null) {
            Instant safeExpiryTime = currentTokenDto.getTokenExpiry().minus(5, ChronoUnit.MINUTES);
            if (Instant.now().isBefore(safeExpiryTime)) {
                System.out.println("Token JWT do DTO ainda é válido. Nenhuma ação necessária.");
                return currentTokenDto; // Retorna o mesmo DTO, pois ele ainda é válido
            }
        }

        // 3. Se não há token ou se ele está prestes a expirar, obtém um novo.
        System.out.println("Token inválido, expirado ou prestes a expirar. Obtendo um novo token...");

        try {
            // A lógica de requisição permanece a mesma
            Map<String, String> payloadMap = Map.of(
                    "client_id", CLIENT_ID,
                    "client_secret", CLIENT_SECRET,
                    "grant_type", "client_credentials"
            );
            String formUrlEncodedPayload = payloadMap.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("https://auth.stackspot.com/realms/%s/protocol/openid-connect/token", REALM )))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formUrlEncodedPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString( ));

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode responseBody = objectMapper.readTree(response.body());

                // Cria um NOVO DTO com o novo token
                String newJwt = responseBody.get("access_token").asText();
                long expiresIn = responseBody.get("expires_in").asLong(3600);
                Instant newExpiry = Instant.now().plusSeconds(expiresIn);

                System.out.println("Token JWT obtido com sucesso. Expira em: " + newExpiry);
                return new EnsureDto(newJwt, newExpiry); // Retorna o novo DTO
            } else {
                System.err.printf("Erro ao obter token JWT. Status: %d, Resposta: %s%n", response.statusCode(), response.body());
                return null; // Falha na autenticação
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro de comunicação ou interrupção ao obter token JWT: " + e.getMessage());
            Thread.currentThread().interrupt(); // Boa prática ao capturar InterruptedException
            return null; // Falha na autenticação
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Gerenciamento do estado do token
        EnsureDto tokenState = null;

        // --- PASSO 1: AUTENTICAR ---
        System.out.println("--- Passo 1: Obtendo token de autenticação ---");
        // CORREÇÃO 3: Passa o objeto 'tokenState' diretamente.
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
                "Crie uma classe Java para representar um Pedido (Order) com id, data e valor total."
        );
        CreateExecutionResponseDto executionResponse = createExecution(executionRequest, tokenState);
        if (executionResponse == null) {
            System.err.println("Falha ao criar a execução. Encerrando teste.");
            return;
        }
        String executionId = executionResponse.getExecutionId();
        System.out.println("Execução criada. ID: " + executionId);

        // --- PASSO 3: OBTER O CONVERSATION ID ---
        System.out.println("\n--- Passo 3: Obtendo o ID da conversação ---");
        System.out.println("Aguardando 5 segundos para a IA processar...");
        Thread.sleep(5000); // Espera simulada

        String conversationId = getConversationId(executionId, tokenState);

        if (conversationId != null) {
            System.out.println("SUCESSO! O ID da conversação é: " + conversationId);
            // Agora você pode usar essa variável 'conversationId' para os próximos passos.
        } else {
            System.err.println("FALHA ao obter o ID da conversação.");
        }
    }
}
