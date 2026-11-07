package com.habibah.springapp.controller;

import com.habibah.springapp.dto.assistant.ChatRequest;
import com.habibah.springapp.dto.assistant.ChatResponse;
import com.habibah.springapp.service.assistant.AssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/assistant")
@RestController
public class AssistantController {
    private final AssistantService assistantService;

    /**
     * Reçoit un message du client et retourne les produits correspondants
     * @param request message envoyé par le client
     * @return réponse de l'assistant
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = assistantService.chat(request.message());
        return ResponseEntity.ok(response);
    }
}
