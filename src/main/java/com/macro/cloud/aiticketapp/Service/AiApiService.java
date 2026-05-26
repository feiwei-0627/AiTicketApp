package com.macro.cloud.aiticketapp.Service;

import com.macro.cloud.aiticketapp.config.AiConfig;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.macro.cloud.aiticketapp.exception.AiBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * @program: SmartTicketService
 * @description: Ai调用服务层
 * @author: feiwei
 * @create: 2026-05-25 21:33
 **/


@Service
@RequiredArgsConstructor
public class AiApiService {

    private final AiConfig aiConfig;
    private final StringRedisTemplate redisTemplate;

    private static final long CACHE_EXPIRE = 10;
    private static final int HTTP_TIMEOUT = 30000;
    private static final int MAX_TRY = 3;
    private final AtomicInteger counter = new AtomicInteger(0); // 单机限流

    // 智能问答（带重试、限流、缓存、内容安全）
    public String chatQuestion(String content) {
        // 1. 限流：单机QPS保护
        if (counter.incrementAndGet() > 50) {
            counter.decrementAndGet();
            throw new AiBusinessException("请求频率过高，请稍后再试");
        }

        // 2. 缓存
        String cacheKey = "ai:chat:" + content;
        String cacheData = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cacheData)) {
            counter.decrementAndGet();
            return cacheData;
        }

        // 3. 构建请求
        JSONObject inputObj = new JSONObject();
        inputObj.put("model", "qwen-turbo");
        inputObj.put("input", JSONObject.of("messages", new Object[]{
                JSONObject.of("role", "user", "content", content)
        }));
        inputObj.put("parameters", JSONObject.of(
                "result_format", "message",
                "temperature", 0.3, // 降低随机性，结果更稳定
                "max_tokens", 1024
        ));
        String body = inputObj.toString();

        String answer = null;

        // 4. 原生重试
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
                try { Thread.sleep(1000); } catch (Exception ignored) {}
            }
        }

        // 5. 内容安全检查
        if (answer == null || answer.length() < 1) {
            counter.decrementAndGet();
            return "AI返回内容异常";
        }

        // 6. 放入缓存
        redisTemplate.opsForValue().set(cacheKey, answer, CACHE_EXPIRE, TimeUnit.MINUTES);
        counter.decrementAndGet();
        return answer;
    }

    // ======================
    /// 【企业级】异步生成工单摘要（指定线程池）
    /// 真实业务逻辑：生成摘要 → 更新数据库 → 记录日志
    // ======================
    @Async("aiTaskExecutor") // 绑定自定义线程池
    public void asyncGenerateTicketSummary(String ticketId, String ticketContent) {
        try {
            System.out.println("【异步线程】开始处理工单摘要，ticketId=" + ticketId);

            // 1. 调用AI生成摘要
            String summary = chatQuestion("请精简总结这段工单，50字以内：" + ticketContent);

            // ======================
            // 2. 【真实业务】更新工单表的摘要字段
            // 模拟：update ticket set summary = ? where id = ?
            // ======================
            System.out.println("【业务入库】工单ID：" + ticketId + "，摘要已更新：" + summary);

            // 3. 记录成功日志
            System.out.println("【完成】工单摘要异步处理成功：" + ticketId);

        } catch (Exception e) {
            // 4. 异常捕获（生产级必须）
            System.err.println("【失败】工单摘要生成失败 ticketId=" + ticketId + "，异常：" + e.getMessage());

            // 可扩展：失败记录到DB、定时任务重试
        }
    }


    // 工单摘要
    public String summaryContent(String ticketText) {
        return chatQuestion("请精简总结这段工单内容，50字以内：" + ticketText);
    }

    // 违规检测
    public String checkIllegal(String text) {
        return chatQuestion("你是内容安全审核员，只回复：合规 / 违规：" + text);
    }
}