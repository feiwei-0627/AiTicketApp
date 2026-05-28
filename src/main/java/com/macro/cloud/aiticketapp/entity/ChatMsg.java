package com.macro.cloud.aiticketapp.entity;

import lombok.Data;

/**
 * @program: AiTicketApp
 * @description: 会话消息
 * @author: feiwei
 * @create: 2026-05-26 15:47
 **/
@Data
public  class ChatMsg {
    private String role; // user / assistant
    private String content;
}

