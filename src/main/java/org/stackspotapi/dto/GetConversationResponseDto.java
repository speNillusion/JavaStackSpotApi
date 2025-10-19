// No arquivo dto/GetConversationResponseDto.java
package org.stackspotapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

// Ignora campos desconhecidos no JSON para evitar erros de parsing
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetConversationResponseDto {
    private String status;
    private String conversationId;
    private JsonNode result; // Usamos JsonNode para flexibilidade, pois o resultado pode ter várias estruturas

    // Construtores, Getters e Setters
    public GetConversationResponseDto() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public JsonNode getResult() {
        return result;
    }

    public void setResult(JsonNode result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "GetConversationResponseDto{" +
                "status='" + status + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", result=" + (result != null ? result.toString() : "null") +
                '}';
    }
}
