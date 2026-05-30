package com.macro.cloud.aiticketapp.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @program: AiTicketApp
 * @description: MyBatis配置类
 * @author: feiwei
 * @create: 2026-05-29 10:42
 **/
@Configuration
@MapperScan("com.macro.cloud.aiticketapp.mapper")
public class MyBatisConfig {
}
