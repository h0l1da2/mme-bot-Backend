package me.mmebot.chat.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, Long> {

    Optional<ChatSessionEntity> findByDiaryId(Long diaryId);

    @Query("""
select cs from ChatSessionEntity cs
join fetch cs.diary d
join fetch d.user
where d.id = :diaryId
""")
    Optional<ChatSessionEntity> findWithDiaryAndUserByDiaryId(Long diaryId);

    @Query("""
select cs from ChatSessionEntity cs
join fetch cs.diary d
join fetch d.user
where cs.id = :id
""")
    Optional<ChatSessionEntity> findWithDiaryAndUser(Long id);
}
