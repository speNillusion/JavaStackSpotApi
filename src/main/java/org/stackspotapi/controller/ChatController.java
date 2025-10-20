// src/main/java/org/stackspotapi/controller/ChatController.java
package org.stackspotapi.controller;

import org.stackspotapi.dto.ChatRequestDto;
import org.stackspotapi.service.AiChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat" ) // Base path for chat related endpoints
public class ChatController {

    // Spring automatically injects AiChatService if it's a Spring component
    public ChatController(AiChatService aiChatService) {
    }

    @PostMapping
    public ResponseEntity<String> askAi(@RequestBody ChatRequestDto requestDto) {
        if (requestDto == null || requestDto.getPrompt() == null || requestDto.getPrompt().isBlank()) {
            return new ResponseEntity<>("Prompt cannot be empty", HttpStatus.BAD_REQUEST);
        }

        try {
            String response = AiChatService.ask(requestDto.getPrompt());
            if (response != null) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Failed to get response from AI service", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Error processing AI chat request: " + e.getMessage());
            return new ResponseEntity<>("An error occurred while communicating with the AI service", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
