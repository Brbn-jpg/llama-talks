package com.LlamaTalks.v1.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.LlamaTalks.v1.exception.ConversationIdNotFound;
import com.LlamaTalks.v1.models.Conversation;
import com.LlamaTalks.v1.models.Message;
import com.LlamaTalks.v1.models.MessageRole;
import com.LlamaTalks.v1.records.ChatRequest;
import com.LlamaTalks.v1.records.ChatResponse;
import com.LlamaTalks.v1.repository.ConverstaionRepository;
import com.LlamaTalks.v1.repository.MessageRepository;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import jakarta.transaction.Transactional;
import reactor.core.publisher.Flux;

@Service
public class ChatServiceImpl implements ChatService{
    private final OllamaChatModel ollama;
    private final StreamingChatModel ollamaStream;
    private final ConverstaionRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ContentRetriever contentRetriever;
    private final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    public ChatServiceImpl(ConverstaionRepository converstaionRepository, 
                            MessageRepository messageRepository, 
                            ContentRetriever contentRetriever, 
                            OllamaChatModel ollama,
                            OllamaStreamingChatModel ollamaStream ){
        this.ollama = ollama;
        this.ollamaStream = ollamaStream;
        this.conversationRepository = converstaionRepository;
        this.messageRepository = messageRepository;
        this.contentRetriever = contentRetriever;
    }

    @Override
    public ChatResponse chat(ChatRequest message){
        if(message == null || message.message() == null || message.message().isEmpty()){
            this.logger.error("Recieved empty chat request");
            throw new IllegalArgumentException("Message cannot be empty");
        }

        String conversationId = prepareConversation(message);
        ChatMemory memory = prepareChatMemory(conversationId);

        // Docs retrieval
        this.logger.info("Retrieving docs for conversation: {}", conversationId);
        List<Content> relevantContents = contentRetriever.retrieve(Query.from(message.message()));
        String context = relevantContents.stream()
                                        .map(content -> content.textSegment().text())
                                        .collect(Collectors.joining("\n\n"));

        if (!context.isEmpty()) {
            this.logger.info("Found context, length: {} chars", context.length());
            SystemMessage systemMessage = SystemMessage.from(
                "Use the following context to answer the user's question:\n\n" + context
            );
            memory.add(systemMessage);
        } else {
            this.logger.warn("context is empty, no docs found");
        }

        UserMessage userMessage = UserMessage.from(message.message());
        memory.add(userMessage);

        saveUserMessage(conversationId, message.message());

        this.logger.info("Sending request to Ollama");
        AiMessage response = this.ollama.chat(memory.messages()).aiMessage();
        this.logger.info("Received response from Ollama, length: {} chars", response.text().length());
        
        memory.add(response);
        saveAiMessage(conversationId, response.text(), context);
        
        return new ChatResponse(response.text(), conversationId);
    }

    @Override
    public Flux<ChatResponse> streamChat(ChatRequest message){
        if(message == null || message.message() == null || message.message().isEmpty()){
            this.logger.error("Recieved empty chat request");
            throw new IllegalArgumentException("Message cannot be empty");
        }

        final String conversationId = prepareConversation(message);
        
        ChatMemory memory = prepareChatMemory(conversationId);

        // Docs retrieval
        this.logger.info("Retrieving docs for conversation: {}", conversationId);
        List<Content> relevantContents = contentRetriever.retrieve(Query.from(message.message()));
        String context = relevantContents.stream()
                                        .map(content -> content.textSegment().text())
                                        .collect(Collectors.joining("\n\n"));

        if (!context.isEmpty()) {
            SystemMessage systemMessage = SystemMessage.from(
                "Use the following context to answer the user's question:\n\n" + context
            );
            memory.add(systemMessage);
        } else {
            this.logger.warn("context is empty, no docs found");
        }

        UserMessage userMessage = UserMessage.from(message.message());
        memory.add(userMessage);
        
        saveUserMessage(conversationId, message.message());

        StringBuilder aiResponse = new StringBuilder();

        Flux<ChatResponse> flux = Flux.create(emitter -> {
            this.logger.info("Sending request to Ollama and streaming the response");
            this.ollamaStream.chat(memory.messages(), new StreamingChatResponseHandler() {
                
                @Override
                public void onPartialResponse(String partialResponse) {
                    emitter.next(new ChatResponse(partialResponse, conversationId));
                    aiResponse.append(partialResponse);
                }

                @Override
                public void onError(Throwable error) {
                    emitter.error(error);
                }

                @Override
                public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse completeResponse) {
                    saveAiMessage(conversationId, aiResponse.toString(), context);
                    emitter.complete();
                }
                
            });
        });
        this.logger.info("Received response from Ollama");
        return flux;
    }

    @Override
    public List<Conversation> getAllMessages(){
        List<Conversation> responses = this.conversationRepository.findAll();

        return responses;
    }

    @Override
    public Conversation getConversationById(String conversationId){
        Conversation conversation = this.conversationRepository.findByConversationId(conversationId);
        if(conversation == null){
            throw new ConversationIdNotFound("Conversation not found");
        }

        this.logger.info("Getting conversation with id: {}", conversationId);
        return conversation;
    }

    @Transactional
    @Override
    public void deleteConverstaion(String conversationId){
        Conversation conversation = this.conversationRepository.findByConversationId(conversationId);
        if(conversation == null){
            throw new ConversationIdNotFound("Conversation not found");
        }

        this.logger.info("Deleting conversation with id: {}", conversationId);
        this.conversationRepository.delete(conversation);
    }

    @Transactional
    @Override
    public void changeTitle(String title, String conversationId){
        Conversation conversation = this.conversationRepository.findByConversationId(conversationId);
        if(conversation == null){
            throw new ConversationIdNotFound("Conversation not found");
        } 
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (title.length() > 63) { 
            throw new IllegalArgumentException("Title too long");
        }

        this.logger.info("Changing title of conversation with id: {}", conversationId);
        conversation.setTitle(title);
    }

    ////////////////////////////////////
    //         Helper methods         //
    ////////////////////////////////////

    @Transactional
    public String prepareConversation(ChatRequest message){
        String conversationId = message.conversationId();
        this.logger.info("Creating new conversation with id: {}", conversationId);

        if(conversationId == null || conversationId.isBlank()){
            conversationId = UUID.randomUUID().toString();
        } else {
            Conversation existing = this.conversationRepository.findByConversationId(conversationId);
            if (existing == null) {
                this.logger.error("Conversation not found: {}", conversationId);
                throw new ConversationIdNotFound("Conversation not found: " + conversationId);
            }
            this.logger.debug("Using existing conversationId: {}", conversationId);
            return conversationId;
        }

        Conversation conversation = this.conversationRepository.findByConversationId(conversationId);
        if(conversation == null){
            conversation = new Conversation();
            conversation.setConversationId(conversationId);
            conversation.setStartedAt(LocalDateTime.now());
            conversation.setTitle(message.message().length() > 64 ? message.message().substring(0, 63) : message.message());
            conversation = this.conversationRepository.save(conversation);
            this.logger.info("Saved new conversation: {}", conversationId);
        }

        return conversationId;
    }

    @Transactional
    public void saveUserMessage(String conversationId, String message){
        if(message == null || message.isBlank()){
            this.logger.error("Attempted to save empty user message");
            throw new IllegalArgumentException("Message cannot be empty");
        }

        Conversation conversation = this.conversationRepository.findByConversationId(conversationId);
        if(conversation == null){
            this.logger.error("Conversation not found when saving user message");
            throw new ConversationIdNotFound("Conversation not found");
        } 

        Message userMsg = new Message();
        userMsg.setConversation(conversation);
        userMsg.setContent(message);
        userMsg.setRole(MessageRole.USER);
        userMsg.setGeneratedAt(LocalDateTime.now());
        this.messageRepository.save(userMsg);
        this.logger.info("Saved user message for conversation: {}", conversationId);
    }

    @Transactional
    public void saveAiMessage(String conversationId, String content, String context) {
        if(content == null || content.isBlank()){
            this.logger.error("Attempted to save empty ai message");
            throw new IllegalArgumentException("AI response cannot be empty");
        }

        Conversation conversation = conversationRepository.findByConversationId(conversationId);
        if(conversation == null){
            this.logger.error("Conversation not found when saving ai message");
            throw new ConversationIdNotFound("Conversation not found");
        } 

        Message aiMsg = new Message();
        aiMsg.setConversation(conversation);
        aiMsg.setContent(content);
        aiMsg.setRole(MessageRole.AI);
        aiMsg.setGeneratedAt(LocalDateTime.now());
        aiMsg.setContext(context != null && context.length() > 255 
                                                    ? context.substring(0, 255) 
                                                    : (context != null ? context : "No context"));
        messageRepository.save(aiMsg);
        this.logger.info("Saved ai message for conversation: {}", conversationId);
    }

    private ChatMemory prepareChatMemory(String conversationId){
        ChatMemory memory = MessageWindowChatMemory.withMaxMessages(20);
        
        List<Message> dbMessages = this.messageRepository.findByConversation_ConversationId(conversationId);
        for(Message m: dbMessages){
            if(m.getRole().equals(MessageRole.USER)){
                memory.add(UserMessage.from(m.getContent()));
            } else {
                memory.add(AiMessage.from(m.getContent()));
            }
        }

        return memory;
    }
}
