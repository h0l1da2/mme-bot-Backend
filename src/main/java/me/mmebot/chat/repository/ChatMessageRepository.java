package me.mmebot.chat.repository;

import java.util.List;
import java.util.Optional;

import me.mmebot.chat.domain.ChatMessageEntity;
import me.mmebot.chat.infrastructure.persistence.ChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    @Query("""
    select cm
    from ChatMessageEntity cm
    join fetch cm.encryptionContext ec
    where cm.chatSession = :chatSession
""")
    List<ChatMessageEntity> findAllByChatSessionWithEnc(ChatSessionEntity chatSession);

    List<ChatMessageEntity> findAllByReplyMsgId(Long replyMsgId);

    boolean existsByChatSessionIdAndSeq(Long chatSessionId, Integer seq);

    Optional<ChatMessageEntity> findByChatSessionIdAndSeq(Long chatSessionId, Integer seq);
}
