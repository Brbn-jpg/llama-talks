package com.LlamaTalks.v1.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.LlamaTalks.v1.models.Embedding;

@Repository
public interface EmbeddingRepository extends JpaRepository<Embedding, UUID> {
    
    @Query(value = """
        SELECT embedding_id, metadata->>'fileName' 
        FROM embeddings
        WHERE metadata->>'fileName' IS NOT NULL
        """, nativeQuery = true)
    List<Object[]> findAllFileNames();
}
