package me.mmebot.chat.api.dto;

import jakarta.validation.constraints.NotNull;

public final class ChatSessionReq {
    public record CreateChatSessionReq(
            @NotNull
            Long diaryId
    ) {}
}
