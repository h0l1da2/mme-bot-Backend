package me.mmebot.chat.application.port.out.persistence;

import me.mmebot.chat.domain.ChatSession;

public interface ChatSessionPersistencePort {

    ChatSession save(ChatSession chatSession);
}
