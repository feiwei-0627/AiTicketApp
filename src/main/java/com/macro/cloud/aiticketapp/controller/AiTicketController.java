package com.macro.cloud.aiticketapp.controller;

import cn.hutool.core.util.IdUtil;
import com.macro.cloud.aiticketapp.common.ApiResponse;
import com.macro.cloud.aiticketapp.entity.TicketAiTask;
import com.macro.cloud.aiticketapp.service.AiTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @program: AiTicketApp
 * @description: 智能工单控制层
 * @author: feiwei
 * @create: 2026-05-25 21:37
 **/


@RestController
@RequestMapping("/ai/ticket")
@RequiredArgsConstructor
public class AiTicketController {

    private final AiTicketService aiTicketService;

    // 单轮问答
    @GetMapping("/chat")
    public ApiResponse<String> chat(@RequestParam String question) {
        return ApiResponse.success(aiTicketService.chatQuestion(question));
    }

    // 多轮对话（会话模式）
    @GetMapping("/session/chat")
    public ApiResponse<String> sessionChat(
            @RequestParam(required = false) String sessionId,
            @RequestParam String question) {
        // 首次对话自动生成会话ID
        String sid = StringUtils.hasText(sessionId) ? sessionId : IdUtil.simpleUUID();
        String res = aiTicketService.chatWithSession(sid, question);
        return ApiResponse.success("会话ID:" + sid + " | 回答:" + res);
    }

    // 同步摘要
    @PostMapping("/summary")
    public ApiResponse<String> summary(@RequestParam String content) {
        return ApiResponse.success(aiTicketService.summaryContent(content));
    }

    // 异步摘要（带任务状态）
    @PostMapping("/async/summary")
    public ApiResponse<String> asyncSummary(
            @RequestParam String ticketId,
            @RequestParam String content) {
        TicketAiTask task = aiTicketService.buildAiTask(ticketId, content);
        aiTicketService.asyncGenerateTicketSummary(task);
        return ApiResponse.success("任务已提交，任务ID：" + task.getTaskId());
    }

    // 内容审核
    @PostMapping("/check")
    public ApiResponse<String> check(@RequestParam String text) {
        return ApiResponse.success(aiTicketService.checkIllegal(text));
    }
}