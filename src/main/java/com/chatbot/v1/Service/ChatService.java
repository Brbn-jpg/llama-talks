package com.chatbot.v1.Service;

import java.util.List;

import com.chatbot.v1.Models.Conversation;
import com.chatbot.v1.Records.ChatRequest;
import com.chatbot.v1.Records.ChatResponse;

import reactor.core.publisher.Flux;

public interface ChatService {
    ChatResponse chat(ChatRequest message);
    Flux<ChatResponse> streamChat(ChatRequest message);
    List<Conversation> getAllMessages();
    Conversation getConversationById(String conversationId);
    void deleteConverstaion(String conversationId);
    void changeTitle(String title, String conversationId);
}
