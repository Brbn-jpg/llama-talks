package com.chatbot.v1.Exception;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private Date timestamp = new Date();
}
