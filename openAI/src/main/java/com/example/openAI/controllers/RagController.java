package com.example.openAI.controllers;

import com.example.openAI.services.DocumentIngestionService;
import com.example.openAI.services.RagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;
    private final ChatClient chatClient;
    private final DocumentIngestionService ingestionService;

    public RagController( RagService ragService,@Qualifier("ragChatClient") ChatClient chatClient,
                          DocumentIngestionService ingestionService){
        this.ragService=ragService;
        this.chatClient=chatClient;
        this.ingestionService=ingestionService;
    }

    @PostMapping("/load")
    public String loadData() {
        return ragService.loadSampleData();
    }

    @GetMapping("/ask")
    public String askQuestion(@RequestHeader String username,
            @RequestParam("question") String question) {

        return chatClient.prompt()
                .advisors((a -> a.param(
                        ChatMemory.CONVERSATION_ID,
                        username)))
                .user(question)
                .call()
                .content();
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(
            @RequestParam("file") MultipartFile file)
            throws Exception {

        int chunks = ingestionService.ingest(file);

        return Map.of(
                "message", "Document indexed successfully",
                "filename", Objects.requireNonNull(file.getOriginalFilename()),
                "chunksStored", chunks
        );
    }
}
