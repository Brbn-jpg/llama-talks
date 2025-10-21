package com.chatbot.v1.exception;

public class ConversationIdNotFound extends RuntimeException{
    public ConversationIdNotFound(String message){
        super(message);
    }
}
