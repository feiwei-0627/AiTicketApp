package com.macro.cloud.aiticketapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macro.cloud.aiticketapp.entity.AiKnowledge;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: AiTicketApp
 * @description: 知识库查询
 * @author: feiwei
 * @create: 2026-05-29 10:04
 **/
@Repository
public interface AiKnowledgeMapper {
    //关键词模糊查询数据库
    List<AiKnowledge> selectByKeywords(@Param("kw")String keywords);
}
