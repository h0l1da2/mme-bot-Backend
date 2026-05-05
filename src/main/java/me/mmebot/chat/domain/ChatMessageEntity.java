package me.mmebot.chat.domain;

import jakarta.persistence.*;
import me.mmebot.common.persistence.DatabaseNames;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mmebot.chat.infrastructure.persistence.ChatSessionEntity;
import me.mmebot.core.domain.EncryptionContextEntity;
import me.mmebot.openai.dto.ChatMessageRole;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = DatabaseNames.Tables.CHAT_MESSAGE, schema = DatabaseNames.Schemas.MME_BOT, indexes = {
        @Index(name = "idx_chat_message_session_seq", columnList = "chat_session_id, seq")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_chat_message_session_seq", columnNames = {"chat_session_id", "seq"})
})
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_session_id", nullable = false)
    private ChatSessionEntity chatSession;

    @Column(nullable = false)
    private Integer seq;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private ChatMessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encryption_context_id", nullable = false)
    private EncryptionContextEntity encryptionContext;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    private ChatMessageEntity replyMsg;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public ChatMessageEntity(ChatSessionEntity chatSession, Integer seq, ChatMessageRole role, String content, EncryptionContextEntity encryptionContext, ChatMessageEntity replyMsg) {
        this.chatSession = chatSession;
        this.seq = seq;
        this.role = role;
        this.content = content;
        this.createdAt = OffsetDateTime.now();
        this.encryptionContext = encryptionContext;
        this.replyMsg = replyMsg;
    }

    public ChatMessageEntity(ChatSessionEntity chatSession, Integer seq, ChatMessageRole role, String content, EncryptionContextEntity encryptionContext) {
        this.chatSession = chatSession;
        this.seq = seq;
        this.role = role;
        this.content = content;
        this.createdAt = OffsetDateTime.now();
        this.encryptionContext = encryptionContext;
    }

    public boolean isUserMsg() {
        return role == ChatMessageRole.USER;
    }

    public ChatMessage toModel() {
        return ChatMessage.builder()
                .id(this.id)
                .chatSessionId(this.chatSession != null ? this.chatSession.getId() : null)
                .seq(this.seq)
                .role(this.role)
                .content(this.content)
                .replyMsgId(this.replyMsg != null ? this.replyMsg.getId() : null)
                .createdAt(this.createdAt)
                .build();
    }

    public void updateReplyMsg(ChatMessageEntity replyMsg) {
        this.replyMsg = replyMsg;
    }
}
