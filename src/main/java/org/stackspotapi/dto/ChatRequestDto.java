// src/main/java/org/stackspotapi/dto/ChatRequestDto.java
package org.stackspotapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank; // Adicionado para validação
import java.util.List;

public class ChatRequestDto {

    private Context context;
    @JsonProperty("user_prompt") // Garante que o nome no JSON seja "user_prompt"
    @NotBlank(message = "User prompt cannot be empty") // Adicionado para validação
    private String userPrompt;

    // Construtor para desserialização JSON (necessário para Jackson)
    public ChatRequestDto() {
    }

    // Construtor completo, útil para criar objetos programaticamente
    public ChatRequestDto(Context context, String userPrompt) {
        this.context = context;
        this.userPrompt = userPrompt;
    }

    public @NotBlank(message = "User prompt cannot be empty") String getPrompt() { return userPrompt; }

    public Context getContext() { return context; }
    public void setContext(Context context) { this.context = context; }
    public String getUserPrompt() { return userPrompt; }
    public void setUserPrompt(String userPrompt) { this.userPrompt = userPrompt; }

    /**
     * Classe interna para representar o objeto "context"
     */
    public static class Context {
        @JsonProperty("conversation_id")
        private String conversationId;
        @JsonProperty("upload_ids")
        private List<String> uploadIds;
        @JsonProperty("agent_id")
        private String agentId;
        @JsonProperty("agent_built_in")
        private boolean agentBuiltIn;
        private String os;
        private String platform;
        @JsonProperty("platform_version")
        private String platformVersion;
        @JsonProperty("stackspot_ai_version")
        private String stackspotAiVersion;

        // Construtor para desserialização JSON (necessário para Jackson)
        public Context() {
        }

        // Um construtor simplificado para os campos mais importantes
        public Context(String conversationId, String agentId) {
            this.conversationId = conversationId;
            this.agentId = agentId;
            // Valores padrão baseados no código Python
            this.uploadIds = List.of(); // Lista vazia
            this.agentBuiltIn = false;
            this.os = "Java HttpClient"; // Podemos simplificar ou usar System.getProperty
            this.platform = "java-app";
            this.platformVersion = "1.0";
            this.stackspotAiVersion = "2.0.0";
        }

        // Getters e Setters para todos os campos...
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        public List<String> getUploadIds() { return uploadIds; }
        public void setUploadIds(List<String> uploadIds) { this.uploadIds = uploadIds; }
        public String getAgentId() { return agentId; }
        public void setAgentId(String agentId) { this.agentId = agentId; }
        public boolean isAgentBuiltIn() { return agentBuiltIn; }
        public void setAgentBuiltIn(boolean agentBuiltIn) { this.agentBuiltIn = agentBuiltIn; }
        public String getOs() { return os; }
        public void setOs(String os) { this.os = os; }
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }
        public String getPlatformVersion() { return platformVersion; }
        public void setPlatformVersion(String platformVersion) { this.platformVersion = platformVersion; }
        public String getStackspotAiVersion() { return stackspotAiVersion; }
        public void setStackspotAiVersion(String stackspotAiVersion) { this.stackspotAiVersion = stackspotAiVersion; }
    }
}
