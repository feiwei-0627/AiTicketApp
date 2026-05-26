package com.macro.cloud.aiticketapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @program: SmartTicketService
 * @description: Ai配置类
 * @author: feiwei
 * @create: 2026-05-25 21:32
 **/
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    private String apiKey;
    private String apiUrl;
}
