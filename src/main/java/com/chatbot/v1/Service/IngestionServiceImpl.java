package com.chatbot.v1.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.chatbot.v1.Records.FileNameDTO;
import com.chatbot.v1.Repository.EmbeddingRepository;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.transaction.Transactional;

@Service
public class IngestionServiceImpl implements IngestionService{
    private final DocumentSplitter splitter;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingRepository embeddingRepository;

    public IngestionServiceImpl(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore, EmbeddingRepository embeddingRepository){
        this.splitter = DocumentSplitters.recursive(2000, 50);
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.embeddingRepository = embeddingRepository;
    }

    @Async
    @Override
    @Transactional
    public CompletableFuture<String> ingestDirectory(String dirPath) {
        String batchId = UUID.randomUUID().toString();

        Path path = Paths.get(dirPath);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new IllegalArgumentException("Directory does not exist: " + dirPath);
        }
        
        ApacheTikaDocumentParser parser = new ApacheTikaDocumentParser();
        List<Document> documents;
        try {
            documents = FileSystemDocumentLoader.loadDocumentsRecursively(dirPath, parser);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load documents from: " + dirPath, e);
        }

        if (documents == null || documents.isEmpty()) {
            throw new IllegalStateException("No PDF files found in directory: " + dirPath);
        }

        Set<String> existingFiles = getAllFileNames().stream()
                                                    .map(FileNameDTO::fileName)
                                                    .collect(Collectors.toSet());

        for (Document document : documents) {
            String fileName = document.metadata().getString("file_name");
        
            if (existingFiles.contains(fileName)) {
                System.out.println("Skipping already ingested file: " + fileName);
                continue;
            }

            List<TextSegment> segments = splitter.split(document);
            System.out.println("Splitting doc: "+document.text());
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
            
            for (int i = 0; i < segments.size(); i++) {
                segments.get(i).metadata()
                    .put("batchId", batchId)
                    .put("fileName", document.metadata().getString("file_name"))
                    .put("chunkIndex", i);
                
                embeddingStore.add(embeddings.get(i), segments.get(i));
            }

            existingFiles.add(fileName);
        }
        
        return CompletableFuture.completedFuture(batchId);
    }

    @Override
    public List<FileNameDTO> getAllFileNames() {
    return embeddingRepository.findAllFileNames().stream()
        .map(row -> new FileNameDTO(
            (UUID) row[0],
            (String) row[1]
        )).toList();
    }
}
