package com.LlamaTalks.v1.Models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "embeddings")
@AllArgsConstructor
@NoArgsConstructor
public class Embedding {
    @Id
    @Column(name = "embedding_id")
    private UUID embeddingId;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    private String embedding;
    
    @Column(name = "text", columnDefinition = "text")
    private String text;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;
    
}