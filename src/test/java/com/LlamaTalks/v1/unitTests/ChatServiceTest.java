package com.LlamaTalks.v1.unitTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.LlamaTalks.v1.exception.ConversationIdNotFound;
import com.LlamaTalks.v1.models.Conversation;
import com.LlamaTalks.v1.models.MessageRole;
import com.LlamaTalks.v1.records.ChatRequest;
import com.LlamaTalks.v1.records.ChatResponse;
import com.LlamaTalks.v1.repository.ConverstaionRepository;
import com.LlamaTalks.v1.repository.MessageRepository;
import com.LlamaTalks.v1.service.ChatServiceImpl;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {
    @Mock
    private ConverstaionRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private OllamaChatModel ollama;
    
    @Mock
    private OllamaStreamingChatModel ollamaStream;
    
    @Mock
    private ContentRetriever contentRetriever;

    @InjectMocks
    private ChatServiceImpl chatServiceImpl;

    @Test
    void shouldReturnCorrectResponseText(){
        ChatRequest request = new ChatRequest("What is 2+2?", null);
        
        when(contentRetriever.retrieve(any(Query.class)))
            .thenReturn(List.of());
        
        when(conversationRepository.findByConversationId(anyString()))
            .thenReturn(null)
            .thenReturn(createMockConversation("conv-123"));
        when(conversationRepository.save(any()))
            .thenReturn(createMockConversation("conv-123"));
        when(messageRepository.findByConversation_ConversationId(anyString()))
            .thenReturn(List.of());
        
        AiMessage aiMsg = AiMessage.from("2+2 equals 4");
        dev.langchain4j.model.chat.response.ChatResponse mockResp = 
            mock(dev.langchain4j.model.chat.response.ChatResponse.class);
        when(mockResp.aiMessage()).thenReturn(aiMsg);
        when(ollama.chat(anyList())).thenReturn(mockResp);
        
        ChatResponse response = chatServiceImpl.chat(request);
        
        assertEquals("2+2 equals 4", response.message());
        assertNotNull(response.conversationId());
    }

    @Test
    void shouldReturnSameConversationIdForExistingConversation(){
        String existingId = "exisiting-conv-123";
        ChatRequest request = new ChatRequest( "What is 6+7?", existingId);

        when(contentRetriever.retrieve(any())).thenReturn(List.of());

        Conversation existingConv = createMockConversation(existingId);
        when(conversationRepository.findByConversationId(existingId)).thenReturn(existingConv);
        when(messageRepository.findByConversation_ConversationId(existingId)).thenReturn(List.of());

        AiMessage aiMsg = AiMessage.from("What is 6+7?");
        dev.langchain4j.model.chat.response.ChatResponse mockResp = 
            mock(dev.langchain4j.model.chat.response.ChatResponse.class);

        when(mockResp.aiMessage()).thenReturn(aiMsg);
        when(ollama.chat(anyList())).thenReturn(mockResp);

        ChatResponse response = chatServiceImpl.chat(request);

        assertEquals(existingId, response.conversationId());
        assertEquals("What is 6+7?", response.message());
    }

    @Test
    void shouldGenerateNewConversationIdWhenNull(){
        ChatRequest request = new ChatRequest("What is 21+67", "");

        when(contentRetriever.retrieve(any())).thenReturn(List.of());

        when(conversationRepository.findByConversationId(anyString())).thenReturn(null)
                                    .thenReturn(createMockConversation("new-conversation-id"));
        when(conversationRepository.save(any())).thenReturn(createMockConversation("new-conversation-id"));
        when(messageRepository.findByConversation_ConversationId(anyString())).thenReturn(List.of());

        AiMessage aiMsg = AiMessage.from("What is 21+67");
        dev.langchain4j.model.chat.response.ChatResponse mockResp = 
            mock(dev.langchain4j.model.chat.response.ChatResponse.class);

        when(mockResp.aiMessage()).thenReturn(aiMsg);
        when(ollama.chat(anyList())).thenReturn(mockResp);

        ChatResponse response = chatServiceImpl.chat(request);

        assertNotNull(response.conversationId());
        assertFalse(response.conversationId().isEmpty());
        assertEquals("What is 21+67", response.message());
    }

    @Test
    void shouldSaveUserMessageWithCorrectContent() {
        ChatRequest request = new ChatRequest("My test message", null);
        
        when(contentRetriever.retrieve(any())).thenReturn(List.of());
        when(conversationRepository.findByConversationId(anyString()))
            .thenReturn(null)
            .thenReturn(createMockConversation("conv-1"));
        when(conversationRepository.save(any()))
            .thenReturn(createMockConversation("conv-1"));
        when(messageRepository.findByConversation_ConversationId(anyString()))
            .thenReturn(List.of());
        
        AiMessage aiMsg = AiMessage.from("Response");
        dev.langchain4j.model.chat.response.ChatResponse mockResp = 
            mock(dev.langchain4j.model.chat.response.ChatResponse.class);
        when(mockResp.aiMessage()).thenReturn(aiMsg);
        when(ollama.chat(anyList())).thenReturn(mockResp);
        
        chatServiceImpl.chat(request);
        
        verify(messageRepository).save(argThat(message -> 
            message.getContent().equals("My test message") && 
            message.getRole() == MessageRole.USER
        ));
    }

    @Test
    void shouldSaveAiMessageWithCorrectContent() {
        ChatRequest request = new ChatRequest("Question", null);
        
        when(contentRetriever.retrieve(any())).thenReturn(List.of());
        when(conversationRepository.findByConversationId(anyString()))
            .thenReturn(null)
            .thenReturn(createMockConversation("conv-1"));
        when(conversationRepository.save(any()))
            .thenReturn(createMockConversation("conv-1"));
        when(messageRepository.findByConversation_ConversationId(anyString()))
            .thenReturn(List.of());
        
        AiMessage aiMsg = AiMessage.from("AI specific answer");
        dev.langchain4j.model.chat.response.ChatResponse mockResp = 
            mock(dev.langchain4j.model.chat.response.ChatResponse.class);
        when(mockResp.aiMessage()).thenReturn(aiMsg);
        when(ollama.chat(anyList())).thenReturn(mockResp);
        
        chatServiceImpl.chat(request);
        
        verify(messageRepository).save(argThat(message -> 
            message.getContent().equals("AI specific answer") && 
            message.getRole() == MessageRole.AI
        ));
    }

    @Test
    void shouldThrowExceptionWhenMessageIsEmpty() {
        ChatRequest request = new ChatRequest("", null);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> chatServiceImpl.chat(request)
        );
        
        assertEquals("Message cannot be empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenConversationNotFound() {
        ChatRequest request = new ChatRequest("Message", "non-existent-id");
        
        when(conversationRepository.findByConversationId("non-existent-id"))
            .thenReturn(null);
        
        ConversationIdNotFound exception = assertThrows(
            ConversationIdNotFound.class, 
            () -> chatServiceImpl.chat(request)
        );
        
        assertTrue(exception.getMessage().contains("non-existent-id"));
    }

    private Conversation createMockConversation(String id) {
        Conversation conv = new Conversation();
        conv.setConversationId(id);
        conv.setTitle("Test");
        conv.setStartedAt(LocalDateTime.now());
        return conv;
    }
}
