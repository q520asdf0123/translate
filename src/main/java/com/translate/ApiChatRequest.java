package com.translate;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

public class ApiChatRequest {

    public static void main(String[] args) {
        String url = "http://localhost:11434/api/chat";
String t = "If you have questions, you are welcome to contact us. One of the best places to get more info and learn more is in the [AFFiNE Community](https://community.affine.pro/) where you can engage with other like-minded individuals.";

// 创建消息数组
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.set("role", "user");
        message.set("content", "翻译下面内容，要求通俗易懂\n"+t);
        messages.add(message);

        // 创建请求体JSON对象
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "gemma:7b-instruct-fp16");
        requestBody.set("messages", messages);
        requestBody.set("stream",false);

        // 发送POST请求
        HttpResponse response = HttpRequest.post(url)
                .body(requestBody.toString())
                .execute();

        // 检查响应状态并打印结果
        if (response.isOk()) {
            String responseBody = response.body();
            JSONObject responseJson = new JSONObject(responseBody);
            String str = responseJson.getJSONObject("message").getStr("content");
            System.out.println("Response: " + str);
        } else {
            System.out.println("Request failed with status code: " + response.getStatus());
        }
    }
}
