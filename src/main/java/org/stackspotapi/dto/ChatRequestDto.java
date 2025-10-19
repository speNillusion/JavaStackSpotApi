// No arquivo dto/ChatRequestDto.java
package org.stackspotapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ChatRequestDto {

    private Context context;
    @JsonProperty("user_prompt") // Garante que o nome no JSON seja "user_prompt"
    private String userPrompt;

    // Construtor, Getters e Setters
    public ChatRequestDto(Context context, String userPrompt) {
        this.context = context;
        this.userPrompt = userPrompt;
    }

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

        // Construtor, Getters e Setters
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
    }
}
