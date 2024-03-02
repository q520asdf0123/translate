package com.translate;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

public class ApiRequest {
    public static void main(String[] args) {
        String url = "http://localhost:11434/api/generate";

        JSONObject json = new JSONObject();
        json.set("model", "gemma:7b-instruct-fp16");
        json.set("prompt", "Translate the following language into Chinese, input what format, output whatever format, and then skip the URL:"+"hello world");
        json.set("stream", false);
        // 发送POST请求
        HttpResponse response = HttpRequest.post(url)
                .body(json.toString())
                .execute();

        if (response.isOk()) {
            String responseBody = response.body();
            JSONObject responseJson = new JSONObject(responseBody);
            String res = responseJson.getJSONObject("message").getStr("content");
            System.out.println("Response JSON: " +res);
        } else {
            System.out.println("Request failed with status code: " + response.getStatus());
        }
    }
}
