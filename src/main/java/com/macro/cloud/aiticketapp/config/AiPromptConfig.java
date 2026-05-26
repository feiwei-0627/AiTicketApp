package com.macro.cloud.aiticketapp.config;

/**
 * @program: SmartTicketService
 * @description: Ai提示词配置
 * @author: feiwei
 * @create: 2026-05-26 09:27
 **/

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.prompt")
public class AiPromptConfig {
    // 工单摘要提示词
    private String ticketSummary;
    // 内容审核提示词
    private String contentCheck;
    // 多轮对话系统预设角色
    private String chatRole;
}
