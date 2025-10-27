package com.LlamaTalks.v1.Models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "message")
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "content", length = 5000)
    private String content;

    @ManyToOne
    @JoinColumn(name = "conversationId")
    @JsonBackReference
    private Conversation conversation;
    
    @Column(name = "messageRole")
    @Enumerated(EnumType.STRING)
    private MessageRole role;

    @Column(name = "generatedAt")
    private LocalDateTime generatedAt;
}
