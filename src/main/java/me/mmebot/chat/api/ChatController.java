package me.mmebot.chat.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mmebot.chat.api.dto.ChatMsgReq.CreateChatMsgReq;
import me.mmebot.chat.api.dto.ChatMsgRes.ChatMsg;
import me.mmebot.chat.api.dto.ChatMsgRes.ChatStreamPayload;
import me.mmebot.chat.api.dto.ChatMsgRes.CreateChatMsgRes;
import me.mmebot.chat.api.dto.ChatMsgRes.StartChatInitRes;
import me.mmebot.chat.api.dto.ChatMsgRes.StartChatRes;
import me.mmebot.chat.application.port.in.command.CreateChatSessionCommand;
import me.mmebot.chat.application.port.in.result.CreateChatSessionResult;
import me.mmebot.chat.service.ChatService;
import me.mmebot.stream.StreamContextStore;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static me.mmebot.chat.api.dto.ChatMsgReq.*;
import static me.mmebot.chat.api.dto.ChatMsgRes.*;
import static me.mmebot.chat.api.dto.ChatSessionReq.*;
import static me.mmebot.chat.api.dto.ChatSessionRes.*;
import static me.mmebot.stream.StreamContextContent.*;

@Slf4j
@RestController
@RequestMapping("${api.base-path}/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final StreamContextStore streamContextStore;

    @PostMapping("/chatSession")
    public CreateChatSessionRes createChatSession(@AuthenticationPrincipal Long userId,
                                                  @RequestBody @Valid CreateChatSessionReq req) {
        CreateChatSessionResult result = chatService.createChatSession(new CreateChatSessionCommand(
                userId,
                req.diaryId()
        ));
        return new CreateChatSessionRes(result.chatSessionId());
    }

    @PostMapping("/{chatSessionId}/messages/start")
    public StartChatInitRes initFirstChatMsg(
            @RequestBody @Valid StartChatReq req,
            @PathVariable("chatSessionId") Long chatSessionId
    ) {
        String streamId = UUID.randomUUID().toString();

        streamContextStore.save(streamId, new FirstChatStreamContext(
                chatSessionId,
                req.userId(),
                LocalDateTime.now()
        ));

        return new StartChatInitRes(streamId);
    }

    @PostMapping("/{chatSessionId}/messages/start/direct")
    public StartChatRes startFirstChatDirect(
            @RequestBody @Valid StartChatReq req,
            @PathVariable("chatSessionId") Long chatSessionId
    ) {
        return chatService.createFirstChatSync(chatSessionId, req);
    }

    @GetMapping(value = "/stream/messages/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatStreamPayload> firstChatStream(@RequestParam String streamId) {
        return chatService.createFirstChat(streamId);
    }

    @GetMapping("/{chatSessionId}/messages")
    public List<ChatMsg> chatMsgs(@AuthenticationPrincipal Long userId,
                                  @PathVariable("chatSessionId") Long chatSessionId) {
        return chatService.getChatMsgs(userId, chatSessionId);
    }

    @GetMapping("/diaries/{diaryId}/messages")
    public List<ChatMsg> chatMsgsByDiaryId(@AuthenticationPrincipal Long userId,
                                           @PathVariable("diaryId") Long diaryId) {
        return chatService.getChatMsgsByDiaryId(userId, diaryId);
    }

//    @PostMapping("/{chatSessionId}/message/start")
//    public StartChatRes startChat(@RequestBody @Valid StartChatReq req,
//                                  @PathVariable("chatSessionId") Long chatSessionId) {
//        return chatService.createFirstChat(chatSessionId, req);
//    }

    @PostMapping("/{chatSessionId}/messages")
    public StartChatMsgRes sendAndStreamMsg(@RequestBody @Valid CreateChatMsgReq req,
                                            @PathVariable("chatSessionId") Long chatSessionId) {
        String streamId = UUID.randomUUID().toString();

        streamContextStore.save(streamId, new ChatStreamContext(
                chatSessionId,
                req.userId(),
                req.replyToMsgId(),
                req.msg(),
                LocalDateTime.now()
        ));

        return new StartChatMsgRes(streamId);
    }

    @PostMapping("/{chatSessionId}/messages/direct")
    public List<CreateChatMsgRes> sendDirectMsg(@RequestBody @Valid CreateChatMsgReq req,
                                                @PathVariable("chatSessionId") Long chatSessionId) {
        return chatService.createChatMessageSync(chatSessionId, req);
    }

    @GetMapping(value = "/stream/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatStreamPayload> chatStream(@RequestParam String streamId) {
        return chatService.createChatMessage(streamId);
    }
//    @PostMapping("/{chatSessionId}/messages")
//    public List<CreateChatMsgRes> sendAndStreamMsg(@RequestBody @Valid CreateChatMsgReq req,
//                                                   @PathVariable("chatSessionId") Long chatSessionId) {
//        return chatService.createChatMessage(chatSessionId, req);
//    }
}
