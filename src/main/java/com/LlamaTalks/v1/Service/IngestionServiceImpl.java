package com.LlamaTalks.v1.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.LlamaTalks.v1.records.FileNameDTO;
import com.LlamaTalks.v1.repository.EmbeddingRepository;

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
    private final Logger logger = LoggerFactory.getLogger(IngestionServiceImpl.class);


    public IngestionServiceImpl(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore, EmbeddingRepository embeddingRepository){
        this.splitter = DocumentSplitters.recursive(2000, 50);
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.embeddingRepository = embeddingRepository;
    }

    @Override
    @Transactional
    public CompletableFuture<String> ingestDirectory(String dirPath) {
        return CompletableFuture.supplyAsync(() -> {
            String batchId = UUID.randomUUID().toString();
            
            this.logger.info("Scanning directory: {}", dirPath);
            Path path = Paths.get(dirPath);
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                this.logger.error("Directory does not exist: {}", dirPath);
                throw new IllegalArgumentException("Directory does not exist: " + dirPath);
            }
        
            ApacheTikaDocumentParser parser = new ApacheTikaDocumentParser();
            List<Document> documents;
            try {
                documents = FileSystemDocumentLoader.loadDocumentsRecursively(dirPath, parser);
            } catch (Exception e) {
                this.logger.error("Failed to load documents from: {}", dirPath);
                throw new RuntimeException("Failed to load documents from: " + dirPath, e);
            }

            if (documents == null || documents.isEmpty()) {
                this.logger.error("No files found in directory: {}", dirPath);
                throw new IllegalStateException("No files found in directory: " + dirPath);
            }

            Set<String> existingFiles = getAllFileNames().stream()
                                                    .map(FileNameDTO::fileName)
                                                    .collect(Collectors.toSet());

            for (Document document : documents) {
                String fileName = document.metadata().getString("file_name");
                this.logger.info("Ingesting document: {}", fileName);

                if (existingFiles.contains(fileName)) {
                    this.logger.info("Skipping already ingested file: {}", fileName);
                    continue;
                }

                List<TextSegment> segments = splitter.split(document);
                List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
                this.logger.info("Embedded segments: {}", embeddings.size());
            
                for (int i = 0; i < segments.size(); i++) {
                    segments.get(i).metadata()
                        .put("batchId", batchId)
                        .put("fileName", document.metadata().getString("file_name"))
                        .put("chunkIndex", i);
                
                    embeddingStore.add(embeddings.get(i), segments.get(i));
                }

                existingFiles.add(fileName);
                this.logger.info("Added {} to existing files", fileName);
            }
        return batchId;  
        });
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
