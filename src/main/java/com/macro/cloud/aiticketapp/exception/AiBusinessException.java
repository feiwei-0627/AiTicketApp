package com.macro.cloud.aiticketapp.exception;

/**
 * @program: SmartTicketService
 * @description: 统一异常
 * @author: feiwei
 * @create: 2026-05-26 08:30
 **/
public class AiBusinessException extends RuntimeException {
    public AiBusinessException(String message) {
        super(message);
    }
}
