package com.LlamaTalks.v1.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.LlamaTalks.v1.records.FileNameDTO;

public interface IngestionService {
    CompletableFuture<String> ingestDirectory(String dirPath);
    List<FileNameDTO> getAllFileNames();
}
