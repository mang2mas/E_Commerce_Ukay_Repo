package com.ukayukay.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.ukayukay.backend.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT m FROM Message m WHERE m.recipient.userId = :recipientId ORDER BY m.createdAt DESC")
    List<Message> findByRecipientId(@Param("recipientId") Integer recipientId);

    @Query("SELECT m FROM Message m WHERE (m.sender.userId = :u1 AND m.recipient.userId = :u2) OR (m.sender.userId = :u2 AND m.recipient.userId = :u1) ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("u1") Integer user1, @Param("u2") Integer user2);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE m.messageId = :messageId AND m.recipient.userId = :recipientId")
    int markAsRead(@Param("messageId") Integer messageId, @Param("recipientId") Integer recipientId);
}
