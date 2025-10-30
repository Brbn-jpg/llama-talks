package com.LlamaTalks.v1.Configuration;

import java.time.Duration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

@Configuration
public class LangchainConfig {

    @Value("${POSTGRES_DB}")
    private String database;

    @Value("${POSTGRES_USER}")
    private String user;

    @Value("${POSTGRES_PASSWORD}")
    private String password;

    @Value("${OLLAMA_URL}")
    private String ollamaUrl;

    @Value("${DIMENSIONS}")
    private int dimension;

    @Value("${EMBEDDING_MODEL}")
    private String embeddingModel;

    @Value("${CHAT_MODEL}")
    private String chatModel;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource){
        return PgVectorEmbeddingStore.builder()
                .host("database")
                .port(5432)
                .database(database)
                .user(user)
                .password(password)
                .table("embeddings")
                .dimension(dimension)
                .createTable(true)
                .dropTableFirst(false)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel(){
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(embeddingModel)
                .timeout(Duration.ofMinutes(5))
                .maxRetries(3)
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore){
        return EmbeddingStoreContentRetriever.builder()
                                    .embeddingModel(embeddingModel)
                                    .embeddingStore(embeddingStore)
                                    .maxResults(3)
                                    .minScore(0.75)
                                    .build();
    }

    @Bean
    public OllamaChatModel ollama(){
        return OllamaChatModel.builder()
                               .modelName(chatModel)
                               .baseUrl(ollamaUrl)
                               .timeout(Duration.ofMinutes(3))
                               .build();
    }

    @Bean
    public OllamaStreamingChatModel ollamaStream(){
        return OllamaStreamingChatModel.builder()
                            .modelName(chatModel)
                            .baseUrl(ollamaUrl)
                            .timeout(Duration.ofMinutes(3))
                            .build();
    }
}
        