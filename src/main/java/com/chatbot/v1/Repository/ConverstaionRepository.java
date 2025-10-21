package com.chatbot.v1.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatbot.v1.Models.Conversation;


public interface ConverstaionRepository extends JpaRepository<Conversation, Long>{
    Conversation findByConversationId(String conversationId);
}
