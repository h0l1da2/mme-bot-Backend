package me.mmebot.chat.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import me.mmebot.bot.domain.BotEntity;
import me.mmebot.chat.domain.ChatSession;
import me.mmebot.core.domain.EncryptionContextEntity;
import me.mmebot.diary.domain.DiaryEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatSessionPersistenceMapper {

    private final EntityManager entityManager;

    public ChatSessionEntity toEntity(ChatSession chatSession) {
        DiaryEntity diary = entityManager.getReference(DiaryEntity.class, chatSession.getDiaryId());
        BotEntity bot = entityManager.getReference(BotEntity.class, chatSession.getBotId());
        EncryptionContextEntity encryptionContext = entityManager.getReference(
                EncryptionContextEntity.class,
                chatSession.getEncryptionContextId()
        );

        return ChatSessionEntity.builder()
                .id(chatSession.getId())
                .diary(diary)
                .bot(bot)
                .status(chatSession.getStatus())
                .sendCount(chatSession.getSendCount())
                .summary(chatSession.getSummary())
                .encryptionContext(encryptionContext)
                .completedAt(chatSession.getCompletedAt())
                .build();
    }

    public ChatSession toDomain(ChatSessionEntity entity) {
        return ChatSession.builder()
                .id(entity.getId())
                .diaryId(entity.getDiary() != null ? entity.getDiary().getId() : null)
                .userId(entity.getDiary() != null && entity.getDiary().getUser() != null
                        ? entity.getDiary().getUser().getId()
                        : null)
                .botId(entity.getBot() != null ? entity.getBot().getId() : null)
                .status(entity.getStatus())
                .sendCount(entity.getSendCount())
                .summary(entity.getSummary())
                .encryptionContextId(entity.getEncryptionContext() != null ? entity.getEncryptionContext().getId() : null)
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
