package com.macro.cloud.aiticketapp.service;

import com.macro.cloud.aiticketapp.entity.AiKnowledge;
import com.macro.cloud.aiticketapp.mapper.AiKnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @program: AiTicketApp
 * @description: 知识库服务
 * @author: feiwei
 * @create: 2026-05-29 13:59
 **/
@Service
@RequiredArgsConstructor
public class AiKnowledgeService {
    private final AiKnowledgeMapper mapper;

    public String search(String question) {
        if (!StringUtils.hasText(question)) return "";
        List<AiKnowledge> list = mapper.selectByKeywords("%" + question.substring(0,2) + "%");
        StringBuilder sb = new StringBuilder();
        list.forEach(k -> sb.append(k.getTitle()).append("：").append(k.getContent()).append("\n"));
        return sb.length() > 1000 ? sb.substring(0,1000) : sb.toString();
    }
}