package com.ukayukay.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.dto.MessageDTO;
import com.ukayukay.backend.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messaging", description = "Buyer-Seller messaging system - Send, receive, and manage messages between buyers and sellers")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    @Operation(summary = "Send a message", description = "Send a new message from one user to another")
    @ApiResponse(responseCode = "200", description = "Message sent successfully")
    @ApiResponse(responseCode = "400", description = "Sender or recipient not found")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody MessageDTO messageDTO) {
        MessageDTO response = messageService.sendMessage(
            messageDTO.getSenderId(),
            messageDTO.getRecipientId(),
            messageDTO.getSubject(),
            messageDTO.getMessageContent()
        );
        if (response == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inbox")
    @Operation(summary = "Get user inbox", description = "Retrieve all messages received by a specific user")
    @ApiResponse(responseCode = "200", description = "Messages retrieved successfully")
    public ResponseEntity<List<MessageDTO>> getInbox(@RequestParam Integer userId) {
        List<MessageDTO> messages = messageService.getInbox(userId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversation")
    @Operation(summary = "Get conversation thread", description = "Retrieve the conversation between two users")
    @ApiResponse(responseCode = "200", description = "Conversation retrieved successfully")
    public ResponseEntity<List<MessageDTO>> getConversation(
            @RequestParam Integer user1,
            @RequestParam Integer user2) {
        List<MessageDTO> messages = messageService.getConversation(user1, user2);
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/mark-as-read")
    @Operation(summary = "Mark message as read", description = "Mark a specific message as read by the recipient")
    @ApiResponse(responseCode = "200", description = "Message marked as read")
    public ResponseEntity<Boolean> markAsRead(
            @RequestParam Integer messageId,
            @RequestParam Integer recipientId) {
        boolean success = messageService.markAsRead(messageId, recipientId);
        return ResponseEntity.ok(success);
    }
}
