package me.mmebot.chat.domain;

import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatSession {

    private final Long id;
    private final Long diaryId;
    private final Long userId;
    private final Long botId;
    private final ChatSessionStatus status;
    private final int sendCount;
    private final String summary;
    private final Long encryptionContextId;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime completedAt;

    public static ChatSession create(Long diaryId, Long userId, Long botId, Long encryptionContextId) {
        return ChatSession.builder()
                .diaryId(diaryId)
                .userId(userId)
                .botId(botId)
                .status(ChatSessionStatus.ACTIVE)
                .sendCount(0)
                .encryptionContextId(encryptionContextId)
                .build();
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    public boolean isActive() {
        return status == ChatSessionStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return status == ChatSessionStatus.COMPLETED;
    }

    public ChatSession complete(String summary, OffsetDateTime completedAt) {
        return ChatSession.builder()
                .id(this.id)
                .diaryId(this.diaryId)
                .userId(this.userId)
                .botId(this.botId)
                .status(ChatSessionStatus.COMPLETED)
                .sendCount(this.sendCount)
                .summary(summary)
                .encryptionContextId(this.encryptionContextId)
                .createdAt(this.createdAt)
                .completedAt(completedAt)
                .build();
    }
}
