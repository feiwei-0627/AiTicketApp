package com.macro.cloud.aiticketapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
/**
 * @program: AiTicketApp
 * @description: 跨域配置
 * @author: feiwei
 * @create: 2026-05-28 16:55
 **/


@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有域名（最关键，解决 63342 跨域）
        config.addAllowedOriginPattern("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有请求方式（GET,POST,PUT,DELETE,OPTIONS）
        config.addAllowedMethod("*");
        // 允许跨域传递Cookie
        config.setAllowCredentials(true);
        // 解决预检请求（OPTIONS）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
