package com.example.openAI.services;


import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final VectorStore vectorStore;

    public RagService(VectorStore vectorStore){
        this.vectorStore=vectorStore;
    }

    public String loadSampleData() {

        List<Document> documents = List.of(

                new Document(
                        "Interstellar is a science fiction film directed by "
                                + "Christopher Nolan. The story involves space travel, "
                                + "a mission to find a habitable planet, and time dilation.",
                        Map.of("category", "movie", "title", "Interstellar")
                ),

                new Document(
                        "The Dark Knight is a superhero crime film directed by "
                                + "Christopher Nolan. Christian Bale plays Batman, and "
                                + "Heath Ledger plays the Joker.",
                        Map.of("category", "movie", "title", "The Dark Knight")
                ),

                new Document(
                        "The ICC Men's Cricket World Cup is an international "
                                + "One Day International cricket tournament organized "
                                + "by the International Cricket Council.",
                        Map.of("category", "cricket", "topic", "World Cup")
                ),

                new Document(
                        "Sachin Tendulkar is a former Indian cricketer. "
                                + "He was the first player to score a double century "
                                + "in men's One Day International cricket.",
                        Map.of("category", "cricket", "topic", "Sachin Tendulkar")
                ),

                new Document(
                        "The Solar System has eight recognized planets. "
                                + "Jupiter is the largest planet, and Saturn is famous "
                                + "for its prominent ring system.",
                        Map.of("category", "gk", "topic", "Solar System")
                ),

                new Document(
                        "The Pacific Ocean is the largest and deepest "
                                + "ocean on Earth.",
                        Map.of("category", "gk", "topic", "Geography")
                )
        );

        vectorStore.add(documents);

        return "Loaded " + documents.size()
                + " sample documents into Qdrant.";
    }
    public VectorStore getVectorStore() {
        return vectorStore;
    }
}
