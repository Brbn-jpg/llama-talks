package com.chatbot.v1.exception;

public class NoMessageException extends RuntimeException {
    public NoMessageException(String message){
        super(message);
    }
}
