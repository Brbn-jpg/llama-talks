package com.LlamaTalks.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LlamaTalks.v1.models.Conversation;


public interface ConverstaionRepository extends JpaRepository<Conversation, Long>{
    Conversation findByConversationId(String conversationId);
}
