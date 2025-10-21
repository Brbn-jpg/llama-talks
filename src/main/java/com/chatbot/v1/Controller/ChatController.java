package com.chatbot.v1.Controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.v1.Models.Conversation;
import com.chatbot.v1.Service.ChatServiceImpl;
import com.chatbot.v1.records.ChatRequest;
import com.chatbot.v1.records.ChatResponse;

import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/chat")
public class ChatController {
    private ChatServiceImpl chatServiceImpl;

    public ChatController(ChatServiceImpl chatServiceImpl){
        this.chatServiceImpl = chatServiceImpl;
    }

    @PostMapping
    public ChatResponse sendMessage(@RequestBody ChatRequest message){
        return this.chatServiceImpl.chat(message);
    }

    @GetMapping
    public List<Conversation> getAllMessages(){
        return this.chatServiceImpl.getAllMessages();
    }

    @GetMapping("/{conversationId}")
    public Conversation getConversationById(@PathVariable String conversationId){
        return this.chatServiceImpl.getConversationById(conversationId);
    }

    @DeleteMapping("/{conversationId}")
    public void deleteConverstaion(@PathVariable String conversationId){
        this.chatServiceImpl.deleteConverstaion(conversationId);
    }

    @PutMapping("/{conversationId}")
    public void changeTitle(@PathVariable String conversationId, @RequestBody String title){
        this.chatServiceImpl.changeTitle(title, conversationId);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest message){
        return this.chatServiceImpl.streamChat(message);
    }
}
