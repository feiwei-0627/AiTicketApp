package com.macro.cloud.aiticketapp.Controller;

import com.macro.cloud.aiticketapp.Service.AiApiService;
import com.macro.cloud.aiticketapp.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
/**
 * @program: SmartTicketService
 * @description: 智能工单控制层
 * @author: feiwei
 * @create: 2026-05-25 21:37
 **/

@RestController
@RequestMapping("/ai/ticket")
@RequiredArgsConstructor
public class AiTicketController {

    private final AiApiService aiApiService;

    @GetMapping("/chat")
    public ApiResponse<String> chat(@RequestParam String question) {
        String answer = aiApiService.chatQuestion(question);
        return ApiResponse.success(answer);
    }

    @GetMapping("/summary")
    public ApiResponse<String> summary(@RequestParam String content) {
        String result = aiApiService.summaryContent(content);
        return ApiResponse.success(result);
    }

    @GetMapping("/check")
    public ApiResponse<String> check(@RequestParam String text) {
        String result = aiApiService.checkIllegal(text);
        return ApiResponse.success(result);
    }

    @GetMapping("/async/summary")
    public ApiResponse<String> asyncSummary(
            @RequestParam String ticketId,
            @RequestParam String content) {
        // 调用异步方法：主线程执行到这里，不会等待方法结束
        aiApiService.asyncGenerateTicketSummary(ticketId, content);
        // 立刻返回结果给前端
        return ApiResponse.success("工单已提交，摘要后台生成中");
    }
}
