package com.chatbot.v1.Exception;

public class ConversationIdNotFound extends RuntimeException{
    public ConversationIdNotFound(String message){
        super(message);
    }
}
