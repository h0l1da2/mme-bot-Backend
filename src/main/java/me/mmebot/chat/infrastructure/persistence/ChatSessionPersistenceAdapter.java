package me.mmebot.chat.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import me.mmebot.chat.application.port.out.persistence.ChatSessionPersistencePort;
import me.mmebot.chat.domain.ChatSession;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatSessionPersistenceAdapter implements ChatSessionPersistencePort {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatSessionPersistenceMapper chatSessionPersistenceMapper;

    @Override
    public ChatSession save(ChatSession chatSession) {
        ChatSessionEntity entity = chatSessionPersistenceMapper.toEntity(chatSession);
        ChatSessionEntity saved = chatSessionRepository.save(entity);
        return chatSessionPersistenceMapper.toDomain(saved);
    }
}
