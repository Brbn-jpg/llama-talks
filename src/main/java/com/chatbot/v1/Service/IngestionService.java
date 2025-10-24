package com.chatbot.v1.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.chatbot.v1.records.FileNameDTO;

public interface IngestionService {
    CompletableFuture<String> ingestDirectory(String dirPath);
    List<FileNameDTO> getAllFileNames();
}
