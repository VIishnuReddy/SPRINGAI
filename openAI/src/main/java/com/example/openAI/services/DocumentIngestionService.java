package com.example.openAI.services;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;

    public DocumentIngestionService(VectorStore vectorStore){
        this.vectorStore=vectorStore;
    }

    public int ingest(MultipartFile file) throws Exception{
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            throw new IllegalArgumentException(
                    "Please upload a valid document."
            );
        }
        String filename = file.getOriginalFilename();

        ByteArrayResource resource =
                new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }
                };

        TikaDocumentReader reader =
                new TikaDocumentReader(resource);

        List<Document> extractedDocuments = reader.read();

        if (extractedDocuments.isEmpty()) {
            throw new IllegalArgumentException(
                    "No text could be extracted from the document."
            );
        }

        // 2. Add metadata to each extracted document
        List<Document> documentsWithMetadata =
                extractedDocuments.stream()
                        .map(doc -> new Document(
                                doc.getText(),
                                Map.of("source", filename)
                        ))
                        .toList();

        // 3. Split into smaller chunks
        TokenTextSplitter splitter =
                TokenTextSplitter.builder().build();

        List<Document> chunks =
                splitter.split(documentsWithMetadata);

        // 4. Store vectors and text in Qdrant
        vectorStore.add(chunks);

        return chunks.size();
    }
}
