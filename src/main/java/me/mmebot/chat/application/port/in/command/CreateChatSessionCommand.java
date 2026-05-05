package me.mmebot.chat.application.port.in.command;

public record CreateChatSessionCommand(
        Long userId,
        Long diaryId
) {
}
