package com.LlamaTalks.v1.service;

import java.util.List;

import com.LlamaTalks.v1.models.Conversation;
import com.LlamaTalks.v1.records.ChatRequest;
import com.LlamaTalks.v1.records.ChatResponse;

import reactor.core.publisher.Flux;

public interface ChatService {
    ChatResponse chat(ChatRequest message);
    Flux<ChatResponse> streamChat(ChatRequest message);
    List<Conversation> getAllMessages();
    Conversation getConversationById(String conversationId);
    void deleteConverstaion(String conversationId);
    void changeTitle(String title, String conversationId);
}
