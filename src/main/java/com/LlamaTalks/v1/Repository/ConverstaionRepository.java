package com.LlamaTalks.v1.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LlamaTalks.v1.Models.Conversation;


public interface ConverstaionRepository extends JpaRepository<Conversation, Long>{
    Conversation findByConversationId(String conversationId);
}
