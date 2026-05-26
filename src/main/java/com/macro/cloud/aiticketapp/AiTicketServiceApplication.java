package com.macro.cloud.aiticketapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiTicketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiTicketServiceApplication.class, args);
    }

}
