// No arquivo dto/CreateExecutionRequestDto.java
package org.stackspotapi.dto;

public class CreateExecutionRequestDto {
    private String slug;
    private Object prompt; // Usamos Object para aceitar String ou um Map/outro objeto
    private String conversationId; // Opcional

    // Construtores
    public CreateExecutionRequestDto(String slug, Object prompt) {
        this.slug = slug;
        this.prompt = prompt;
    }

    public CreateExecutionRequestDto(String slug, Object prompt, String conversationId) {
        this.slug = slug;
        this.prompt = prompt;
        this.conversationId = conversationId;
    }

    // Getters e Setters
    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Object getPrompt() {
        return prompt;
    }

    public void setPrompt(Object prompt) {
        this.prompt = prompt;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
