package me.mmebot.chat.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mmebot.bot.domain.BotEntity;
import me.mmebot.chat.domain.ChatSessionStatus;
import me.mmebot.common.persistence.DatabaseNames;
import me.mmebot.core.domain.EncryptionContextEntity;
import me.mmebot.diary.domain.DiaryEntity;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = DatabaseNames.Tables.CHAT_SESSION, schema = DatabaseNames.Schemas.MME_BOT, indexes = {
        @Index(name = "idx_chat_session_diary_id", columnList = "diary_id", unique = true)
})
public class ChatSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_session_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false, unique = true)
    private DiaryEntity diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_id", nullable = false)
    private BotEntity bot;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private ChatSessionStatus status;

    @Column(name = "send_count", nullable = false)
    private int sendCount;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encryption_context_id", nullable = false)
    private EncryptionContextEntity encryptionContext;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

}
