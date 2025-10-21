package com.chatbot.v1.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoMessageException.class)
    public ResponseEntity<ErrorResponse> handleNoMessageException(NoMessageException exception, HttpServletRequest request){
        logger.error("Messege not found: {}", exception.getMessage());

        ErrorResponse error = new ErrorResponse();
        error.setMessage("Messege not found");
        error.setStatus(HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<ErrorResponse>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConversationIdNotFound.class)
    public ResponseEntity<ErrorResponse> handleConversationIdNotFound(NoMessageException exception, HttpServletRequest request){
        logger.error("Conversation not found: {}", exception.getMessage());

        ErrorResponse error = new ErrorResponse();
        error.setMessage("Conversation not found");
        error.setStatus(HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<ErrorResponse>(error, HttpStatus.NOT_FOUND);
    }
}
