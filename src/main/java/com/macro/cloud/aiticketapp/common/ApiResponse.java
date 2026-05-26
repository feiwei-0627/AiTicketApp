package com.macro.cloud.aiticketapp.common;


import lombok.Data;

/**
 * @program: AiTicketApp
 * @description: 封装接口统一返回对象
 * @author: feiwei
 * @create: 2026-05-26 08:28
 **/



@Data
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setCode(200);
        res.setMsg("success");
        res.setData(data);
        return res;
    }

    public static <T> ApiResponse<T> fail(String msg) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setCode(500);
        res.setMsg(msg);
        return res;
    }
}