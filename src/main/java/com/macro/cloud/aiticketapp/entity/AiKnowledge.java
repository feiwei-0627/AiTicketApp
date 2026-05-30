package com.macro.cloud.aiticketapp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @program: AiTicketApp
 * @description: 知识库
 * @author: feiwei
 * @create: 2026-05-29 09:29
 **/
@Data
public class AiKnowledge {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String keywords;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
