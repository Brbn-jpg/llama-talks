package com.chatbot.v1.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.chatbot.v1.Models.Embedding;

@Repository
public interface EmbeddingRepository extends JpaRepository<Embedding, UUID> {
    
    @Query(value = """
        SELECT embedding_id, metadata->>'fileName' 
        FROM embeddings
        WHERE metadata->>'fileName' IS NOT NULL
        """, nativeQuery = true)
    List<Object[]> findAllFileNames();

    @Query(value = """
        SELECT EXISTS(
            SELECT 1 FROM embeddings 
            WHERE metadata->>'fileName' = :fileName
        )
        """, nativeQuery = true)
    boolean existsByFileName(String fileName);
}
