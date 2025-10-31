package com.LlamaTalks.v1.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.LlamaTalks.v1.records.FileNameDTO;
import com.LlamaTalks.v1.service.IngestionServiceImpl;

@RestController
@RequestMapping("/ingestion")
public class IngestionController {

    private IngestionServiceImpl ingestionServiceImpl;

    public IngestionController(IngestionServiceImpl ingestionServiceImpl){
        this.ingestionServiceImpl = ingestionServiceImpl;
    }

    @PostMapping
    public ResponseEntity<String> ingestFiles(@RequestParam String filePath) throws Exception{
        CompletableFuture<String> batchId = ingestionServiceImpl.ingestDirectory(filePath);
        return ResponseEntity.accepted().body("Files ingested. BatchId: "+batchId);
    }

    @GetMapping
    public List<FileNameDTO> getAllFileNames(){
        return ingestionServiceImpl.getAllFileNames();
    }
}
