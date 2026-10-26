package com.example.openAI.configs;

import com.example.openAI.OpenAiApplication;
import com.example.openAI.advisors.TokenUsageAuditAdvisor;

import com.example.openAI.tools.TimeTools;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class ChatConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory,
                                 SemanticCacheAdvisor semanticCacheAdvisor, TimeTools timeTools,
                                 ToolCallbackProvider toolCallbackProvider)
            throws IOException {

        ClassPathResource resource =
                new ClassPathResource("prompts/spring_instructor.txt");

        String instructorPrompt;

        try (var inputStream = resource.getInputStream()) {
            instructorPrompt = StreamUtils.copyToString(
                    inputStream, StandardCharsets.UTF_8);
        }
    var options = OpenAiChatOptions.builder().model("gpt-5.4-mini")
            .temperature(0.8);
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return builder
                .defaultSystem(instructorPrompt)
                .defaultOptions(options)
                .defaultTools(timeTools, toolCallbackProvider)
                .defaultAdvisors(new TokenUsageAuditAdvisor(), memoryAdvisor, semanticCacheAdvisor)
                .build();
    }



    @Bean("ragChatClient")
    public ChatClient ragChatClient(
            ChatModel chatModel,
            ChatMemory chatMemory,
            SemanticCacheAdvisor semanticCacheAdvisor,
            VectorStore vectorStore) {

        MessageChatMemoryAdvisor memoryAdvisor =
                MessageChatMemoryAdvisor.builder(chatMemory).build();

        OpenAiChatOptions transformerOptions =
                OpenAiChatOptions.builder()
                        .model("gpt-5.4-mini")
                        .temperature(0.0)
                        .build();

        // Builder used by all query transformers and the expander
        ChatClient.Builder transformerClientBuilder =
                ChatClient.builder(chatModel)
                        .defaultOptions(
                                OpenAiChatOptions.builder()
                                        .model("gpt-5.4-mini")
                                        .temperature(0.0)
                        )
                        .build()
                        .mutate();

        // 1. Compress follow-up questions
        CompressionQueryTransformer compression =
                CompressionQueryTransformer.builder()
                        .chatClientBuilder(transformerClientBuilder)
                        .build();

        // 2. Rewrite the query for better retrieval
        RewriteQueryTransformer rewrite =
                RewriteQueryTransformer.builder()
                        .chatClientBuilder(transformerClientBuilder)
                        .build();

        // 3. Translate queries into English
        TranslationQueryTransformer translation =
                TranslationQueryTransformer.builder()
                        .chatClientBuilder(transformerClientBuilder)
                        .targetLanguage("english")
                        .build();

        // Pre-retrieval + retrieval pipeline
        Advisor ragAdvisor =
                RetrievalAugmentationAdvisor.builder()
                        .queryTransformers(
                                compression,
                                rewrite,
                                translation
                        )
                        .documentRetriever(
                                VectorStoreDocumentRetriever.builder()
                                        .vectorStore(vectorStore)
                                        .similarityThreshold(0.50)
                                        .topK(5)
                                        .build()
                        )
                        .build();

        return ChatClient.builder(chatModel)
                .defaultSystem("""
                    You are a knowledge assistant for the
                    information stored in the knowledge base.

                    Answer using the retrieved context only.
                    If the context does not contain enough
                    information to answer, say that the
                    available data does not provide enough
                    information.

                    Do not invent facts.
                    """)
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("gpt-5.4-mini")
                                .temperature(0.2)
                )
                .defaultAdvisors(
                        new TokenUsageAuditAdvisor(),
                        memoryAdvisor,
                        ragAdvisor
                        //semanticCacheAdvisor
                )
                .build();
    }

    @Bean
    public org.springframework.boot.ApplicationRunner logMcpTools(
            ToolCallbackProvider toolCallbackProvider) {

        return args -> {
            System.out.println("===== DISCOVERED MCP TOOLS =====");

            for (var tool : toolCallbackProvider.getToolCallbacks()) {
                System.out.println(tool.getToolDefinition().name());
            }

            System.out.println("================================");
        };
    }

}
