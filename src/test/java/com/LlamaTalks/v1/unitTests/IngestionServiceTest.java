package com.LlamaTalks.v1.unitTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.LlamaTalks.v1.Records.FileNameDTO;
import com.LlamaTalks.v1.Repository.EmbeddingRepository;
import com.LlamaTalks.v1.Service.IngestionServiceImpl;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YourServiceTest {

    @Mock
    private DocumentSplitter splitter;
    
    @Mock
    private EmbeddingModel embeddingModel;
    
    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;
    
    @Mock
    private EmbeddingRepository embeddingRepository;
    
    @InjectMocks
    private IngestionServiceImpl service;
    
    @Test
    void ingestDirectory_shouldThrowExceptionForNonExistentDirectory() {
        String fakePath = "/writing/tests/isnt/enjoyable/";
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.ingestDirectory(fakePath);
        });
    }
    
    @Test
    void getAllFileNames_shouldReturnListOfFiles() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Object[] row1 = {id1, "document1.pdf"};
        Object[] row2 = {id2, "document2.pdf"};
        List<Object[]> mockData = Arrays.asList(row1, row2);
        
        when(embeddingRepository.findAllFileNames()).thenReturn(mockData);
        
        List<FileNameDTO> result = service.getAllFileNames();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("document1.pdf", result.get(0).fileName());
        assertEquals("document2.pdf", result.get(1).fileName());
    }
    
    @Test
    void getAllFileNames_shouldReturnEmptyListWhenNoFiles() {
        when(embeddingRepository.findAllFileNames()).thenReturn(Collections.emptyList());
        
        List<FileNameDTO> result = service.getAllFileNames();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}