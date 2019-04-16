package mvc.controller;

import annotation.MythAutowired;
import annotation.MythRequestMapping;
import annotation.MythRestController;
import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.HttpHeaderNames;
import mvc.service.impl.TestServiceImpl;
import server.HttpMythRequest;
import server.HttpMythResponse;

import java.util.HashMap;

@MythRestController
public class TestController {
    @MythAutowired
    private TestServiceImpl service;
    @MythRequestMapping("/mm.do")
    public String test(HttpMythResponse response, HttpMythRequest request) {
        String result;
        try {
            result = "你提交的是" + request.getParameter("gender")
                    + service.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "500";
        }
        return result;
    }
}
