package com.LlamaTalks.v1.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LlamaTalks.v1.Models.Message;

public interface MessageRepository extends JpaRepository<Message, Long>{
    List<Message> findByConversation_ConversationId(String conversationId);
}