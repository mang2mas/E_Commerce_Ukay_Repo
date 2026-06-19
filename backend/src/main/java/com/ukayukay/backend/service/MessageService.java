package com.ukayukay.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.dto.MessageDTO;
import com.ukayukay.backend.entity.Message;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.MessageRepository;
import com.ukayukay.backend.repository.UserRepository;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public MessageDTO sendMessage(Integer senderId, Integer recipientId, String subject, String content) {
        Optional<User> s = userRepository.findById(senderId);
        Optional<User> r = userRepository.findById(recipientId);
        if (s.isEmpty() || r.isEmpty()) {
            return null;
        }
        Message m = new Message();
        m.setSender(s.get());
        m.setRecipient(r.get());
        m.setSubject(subject);
        m.setMessageContent(content);
        m.setIsRead(false);
        Message saved = messageRepository.save(m);
        return toDto(saved);
    }

    public List<MessageDTO> getInbox(Integer userId) {
        return messageRepository.findByRecipientId(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<MessageDTO> getConversation(Integer user1, Integer user2) {
        return messageRepository.findConversation(user1, user2).stream().map(this::toDto).collect(Collectors.toList());
    }

    public boolean markAsRead(Integer messageId, Integer recipientId) {
        int updated = messageRepository.markAsRead(messageId, recipientId);
        return updated > 0;
    }

    private MessageDTO toDto(Message m) {
        MessageDTO dto = new MessageDTO();
        dto.setMessageId(m.getMessageId());
        dto.setSenderId(m.getSender() != null ? m.getSender().getUserId() : null);
        dto.setRecipientId(m.getRecipient() != null ? m.getRecipient().getUserId() : null);
        dto.setSenderName(m.getSender() != null ? m.getSender().getFullName() : null);
        dto.setRecipientName(m.getRecipient() != null ? m.getRecipient().getFullName() : null);
        dto.setSubject(m.getSubject());
        dto.setMessageContent(m.getMessageContent());
        dto.setIsRead(m.getIsRead());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
