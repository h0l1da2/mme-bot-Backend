package me.mmebot.chat.queue;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mmebot.chat.config.ChatPersistenceQueueProperties;
import me.mmebot.chat.domain.ChatMessageEntity;
import me.mmebot.chat.infrastructure.persistence.ChatSessionEntity;
import me.mmebot.chat.infrastructure.persistence.ChatSessionRepository;
import me.mmebot.chat.repository.ChatMessageRepository;
import me.mmebot.chat.service.ChatService;
import me.mmebot.user.domain.UserEntity;
import me.mmebot.user.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatPersistenceTaskWorker {

    private final ChatPersistenceQueueService queueService;
    private final ChatPersistenceQueueProperties properties;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final ChatService chatService;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(fixedDelayString = "${chat.persistence.worker-delay:5000}")
    public void processQueue() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            for (int i = 0; i < properties.batchSize(); i++) {
                ChatPersistenceTaskPayload payload = queueService.pop();
                if (payload == null) {
                    break;
                }
                handlePayload(payload);
            }
        } finally {
            running.set(false);
        }
    }

    private void handlePayload(ChatPersistenceTaskPayload payload) {
        if (payload.retryCount() >= properties.maxRetry()) {
            queueService.moveToDeadLetter(payload);
            return;
        }

        try {
            switch (payload.taskType()) {
                case FIRST_MESSAGE -> handleFirstMessage(payload);
                case CHAT_MESSAGE_PAIR -> handleChatMessagePair(payload);
                default -> log.warn("Unknown chat persistence task {}", payload.taskType());
            }
        } catch (Exception e) {
            log.error("Failed to process chat persistence task {} for session {}: {}",
                    payload.taskType(),
                    payload.chatSessionId(),
                    e.getMessage(),
                    e);
            ChatPersistenceTaskPayload retried = payload.incrementRetry(truncateReason(e.getMessage()));
            if (retried.retryCount() > properties.maxRetry()) {
                queueService.moveToDeadLetter(retried);
            } else {
                queueService.enqueue(retried);
            }
        }
    }

    private void handleFirstMessage(ChatPersistenceTaskPayload payload) {
        if (payload.assistantMessageSeq() == null) {
            throw new IllegalStateException("assistantMessageSeq is required for FIRST_MESSAGE tasks");
        }
        if (chatMessageRepository.existsByChatSessionIdAndSeq(payload.chatSessionId(), payload.assistantMessageSeq())) {
            log.debug("First message already persisted for session {} - skipping", payload.chatSessionId());
            return;
        }

        ChatSessionEntity chatSession = chatSessionRepository.findById(payload.chatSessionId())
                .orElseThrow(() -> new IllegalStateException("Chat session not found: " + payload.chatSessionId()));
        UserEntity user = userService.getActiveUser(payload.userId());
        chatService.saveFirstMessage(chatSession, user, payload.assistantContent());
        log.info("Persisted FIRST_MESSAGE task for session {}", payload.chatSessionId());
    }

    private void handleChatMessagePair(ChatPersistenceTaskPayload payload) {
        if (payload.replyMessageId() == null || payload.userMessageSeq() == null || payload.assistantMessageSeq() == null) {
            throw new IllegalStateException("CHAT_MESSAGE_PAIR payload is missing required fields");
        }

        boolean userExists = chatMessageRepository.existsByChatSessionIdAndSeq(payload.chatSessionId(), payload.userMessageSeq());
        boolean assistantExists = chatMessageRepository.existsByChatSessionIdAndSeq(payload.chatSessionId(), payload.assistantMessageSeq());
        if (userExists || assistantExists) {
            log.debug("Chat message pair already persisted for session {} seqs [{}, {}] - skipping",
                    payload.chatSessionId(),
                    payload.userMessageSeq(),
                    payload.assistantMessageSeq());
            return;
        }

        ChatSessionEntity chatSession = chatSessionRepository.findById(payload.chatSessionId())
                .orElseThrow(() -> new IllegalStateException("Chat session not found: " + payload.chatSessionId()));
        UserEntity user = userService.getActiveUser(payload.userId());
        ChatMessageEntity replyMsg = chatMessageRepository.findById(payload.replyMessageId())
                .orElseThrow(() -> new IllegalStateException("Reply message not found: " + payload.replyMessageId()));

        chatService.saveChatMessagePair(chatSession, user, replyMsg, payload.userContent(), payload.assistantContent());
        log.info("Persisted CHAT_MESSAGE_PAIR task for session {} seqs [{}, {}]",
                payload.chatSessionId(),
                payload.userMessageSeq(),
                payload.assistantMessageSeq());
    }

    private String truncateReason(String reason) {
        if (reason == null) {
            return "unknown";
        }
        return reason.length() > 512 ? reason.substring(0, 512) : reason;
    }
}
