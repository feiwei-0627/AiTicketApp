package com.macro.cloud.aiticketapp.entity;

import lombok.Data;

import java.util.List;

/**
 * @program: AiTicketApp
 * @description: 多轮会话
 * @author: feiwei
 * @create: 2026-05-26 09:29
 **/

/**
 * 多轮对话会话实体
 */
@Data
public class ChatSession {
    // 会话ID
    private String sessionId;
    // 对话历史：角色+内容
    private List<ChatMsg> msgList;
    // 会话创建时间
    private Long createTime;
}