package com.macro.cloud.aiticketapp.service;

import com.macro.cloud.aiticketapp.config.AiConfig;
import com.macro.cloud.aiticketapp.config.AiPromptConfig;
import com.macro.cloud.aiticketapp.entity.ChatMsg;
import com.macro.cloud.aiticketapp.entity.ChatSession;
import com.macro.cloud.aiticketapp.entity.TicketAiTask;
import com.macro.cloud.aiticketapp.exception.AiBusinessException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: AiTicketApp
 * @description: Ai调用服务层
 * @author: feiwei
 * @create: 2026-05-25 21:33
 **/




@Service
@RequiredArgsConstructor
public class AiTicketService {

    private final AiConfig aiConfig;
    private final AiPromptConfig promptConfig;
    private final StringRedisTemplate redisTemplate;

    private static final long CACHE_EXPIRE = 10;
    private static final int HTTP_TIMEOUT = 30000;
    private static final int MAX_TRY = 3;
    private static final long SESSION_EXPIRE = 1800; // 会话缓存30分钟
    private final AtomicInteger counter = new AtomicInteger(0);

    // ===================== 基础单轮问答（原有逻辑保留） =====================
    public String chatQuestion(String content) {
        //限流QPS50
        if (counter.incrementAndGet() > 50) {
            counter.decrementAndGet();
            throw new AiBusinessException("请求频率过高，请稍后再试");
        }
        //redis缓存查询，有直接返回
        String cacheKey = "ai:chat:single:" + content;
        String cacheData = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cacheData)) {
            counter.decrementAndGet();
            return cacheData;
        }

        JSONObject inputObj = buildBaseRequest(content);
        String body = inputObj.toString();
        String answer = null;

        //失败重试两次
        for (int i = 0; i < MAX_TRY; i++) {
            try {
                HttpResponse response = HttpRequest.post(aiConfig.getApiUrl())
                        .header("Authorization", "Bearer " + aiConfig.getApiKey())
                        .header("Content-Type", "application/json")
                        .body(body)
                        .timeout(HTTP_TIMEOUT)
                        .execute();

                if (!response.isOk())
                    throw new AiBusinessException("AI服务异常：" + response.getStatus());

                JSONObject resultJson = JSON.parseObject(response.body());
                answer = resultJson.getJSONObject("output")
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
                break;
            } catch (Exception e) {
                if (i == MAX_TRY - 1) {
                    counter.decrementAndGet();
                    return "AI服务繁忙，请稍后重试";
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {}
            }
        }

        redisTemplate.opsForValue().set(cacheKey, answer, CACHE_EXPIRE, TimeUnit.MINUTES);
        counter.decrementAndGet();
        return answer;
    }

    // ===================== 新增：多轮对话（会话上下文） =====================
    public String chatWithSession(String sessionId, String userContent) {
        String sessionKey = "ai:session:" + sessionId;
        ChatSession session;

        // 1. 查询会话历史
        String sessionStr = redisTemplate.opsForValue().get(sessionKey);
        if (StringUtils.hasText(sessionStr)) {
            session = JSON.parseObject(sessionStr, ChatSession.class);
        } else {
            // 新建会话，初始化角色设定
            session = new ChatSession();
            session.setSessionId(sessionId);
            session.setCreateTime(System.currentTimeMillis());
            List<ChatMsg> msgList = new ArrayList<>();
            ChatMsg roleMsg = new ChatMsg();
            roleMsg.setRole("system");
            roleMsg.setContent(promptConfig.getChatRole());
            msgList.add(roleMsg);
            session.setMsgList(msgList);
        }

        // 2. 追加当前用户提问
        ChatMsg userMsg = new ChatMsg();
        userMsg.setRole("user");
        userMsg.setContent(userContent);
        session.getMsgList().add(userMsg);

        // 3. 组装多轮请求体
        JSONObject inputObj = new JSONObject();
        inputObj.put("model", "qwen-turbo");
        inputObj.put("input", JSONObject.of("messages", session.getMsgList()));
        inputObj.put("parameters", JSONObject.of("result_format", "message", "temperature", 0.3));

        String answer = callAiApi(inputObj.toString());

        // 4. 追加AI回复到会话
        ChatMsg aiMsg = new ChatMsg();
        aiMsg.setRole("assistant");
        aiMsg.setContent(answer);
        session.getMsgList().add(aiMsg);

        // 5. 更新会话缓存
        redisTemplate.opsForValue().set(sessionKey, JSON.toJSONString(session), SESSION_EXPIRE, TimeUnit.SECONDS);
        return answer;
    }

    // ===================== 新增：异步工单摘要（带任务状态管理） =====================
    @Async("aiTaskExecutor")
    public void asyncGenerateTicketSummary(TicketAiTask task) {
        try {
            // 更新状态：处理中
            task.setStatus(1);
            System.out.println("异步任务开始执行，任务ID：" + task.getTaskId());

            // 使用配置化提示词
            String prompt = promptConfig.getTicketSummary() + task.getContent();
            String summary = chatQuestion(prompt);

            // 任务处理成功
            task.setStatus(2);
            task.setSummary(summary);
            task.setFinishTime(System.currentTimeMillis());
            System.out.println("工单" + task.getTicketId() + "摘要生成成功：" + summary);

            // 此处模拟：更新数据库 task 记录

        } catch (Exception e) {
            task.setStatus(3);
            task.setErrorMsg(e.getMessage());
            task.setFinishTime(System.currentTimeMillis());
            System.err.println("工单AI任务失败，任务ID：" + task.getTaskId() + "，原因：" + e.getMessage());
            // 可扩展：失败任务存入失败队列，定时任务重试
        }
    }

    // ===================== 工具方法：统一调用AI接口 + 限流/重试 =====================
    private String callAiApi(String requestBody) {
        if (counter.incrementAndGet() > 50) {
            counter.decrementAndGet();
            throw new AiBusinessException("请求过载");
        }
        String answer = null;
        for (int i = 0; i < MAX_TRY; i++) {
            try {
                HttpResponse response = HttpRequest.post(aiConfig.getApiUrl())
                        .header("Authorization", "Bearer " + aiConfig.getApiKey())
                        .header("Content-Type", "application/json")
                        .body(requestBody)
                        .timeout(HTTP_TIMEOUT)
                        .execute();
                if (!response.isOk()) throw new RuntimeException("接口异常");
                JSONObject resultJson = JSON.parseObject(response.body());
                answer = resultJson.getJSONObject("output")
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
                break;
            } catch (Exception e) {
                if (i == MAX_TRY - 1) {
                    counter.decrementAndGet();
                    throw new AiBusinessException("AI调用多次失败");
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {}
            }
        }
        counter.decrementAndGet();
        return answer;
    }

    // 构建基础请求体
    private JSONObject buildBaseRequest(String content) {
        JSONObject inputObj = new JSONObject();
        inputObj.put("model", "qwen-turbo");
        inputObj.put("input", JSONObject.of("messages", new Object[]{
                JSONObject.of("role", "user", "content", content)
        }));
        inputObj.put("parameters", JSONObject.of("result_format", "message", "temperature", 0.3, "max_tokens", 1024));
        return inputObj;
    }

    // 同步摘要（读取配置提示词）
    public String summaryContent(String ticketText) {
        String prompt = promptConfig.getTicketSummary() + ticketText;
        return chatQuestion(prompt);
    }

    // 内容审核（读取配置提示词）
    public String checkIllegal(String text) {
        String prompt = promptConfig.getContentCheck() + text;
        return chatQuestion(prompt);
    }

    // 初始化异步任务对象
    public TicketAiTask buildAiTask(String ticketId, String content) {
        TicketAiTask task = new TicketAiTask();
        task.setTaskId(IdUtil.simpleUUID());
        task.setTicketId(ticketId);
        task.setStatus(0);
        task.setContent(content);
        task.setCreateTime(System.currentTimeMillis());
        return task;
    }
}