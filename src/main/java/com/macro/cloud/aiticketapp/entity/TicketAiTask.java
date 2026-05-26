package com.macro.cloud.aiticketapp.entity;


import lombok.Data;
/**
 * @program: AiTicketApp
 * @description: 智能工单
 * @author: feiwei
 * @create: 2026-05-26 09:32
 **/

/**
 * 工单AI异步任务状态（模拟数据库表）
 * 状态：0-待处理 1-处理中 2-处理成功 3-处理失败
 */
@Data
public class TicketAiTask {
    private String taskId;
    private String ticketId;
    private Integer status;
    private String content;
    private String summary;
    private String errorMsg;
    private Long createTime;
    private Long finishTime;
}
