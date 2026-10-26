package com.example.openAI.controllers;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController( @Qualifier("chatClient") ChatClient chatClient){
        this.chatClient= chatClient;
    }

    @GetMapping("/chat")
    public String getMessage(@RequestParam("message") String message,
                                   @RequestHeader("username") String username){
        return chatClient.prompt()
                .user(message)
                .advisors((a -> a.param(
                        ChatMemory.CONVERSATION_ID,
                        username)))
                .call().content();
    }
}
