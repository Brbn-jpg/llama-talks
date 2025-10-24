package com.chatbot.v1.Controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.v1.Records.FileNameDTO;
import com.chatbot.v1.Service.IngestionServiceImpl;

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
        return ResponseEntity.accepted().body(batchId.toString());
    }

    @GetMapping
    public List<FileNameDTO> getAllFileNames(){
        return ingestionServiceImpl.getAllFileNames();
    }
}
