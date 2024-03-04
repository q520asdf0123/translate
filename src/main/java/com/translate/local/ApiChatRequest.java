package com.translate.local;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

public class ApiChatRequest {
    private static final String url = "http://localhost:11434/api/chat";

    public static void main(String[] args) {
        String text = "| Name |  |  |\n" +
                "| --- | --- | --- |\n" +
                "| [@affine/component](https://github.com/toeverything/AFFiNE/blob/canary/packages/frontend/component) | AFFiNE Component Resources | [![](https://camo.githubusercontent.com/c2d01cfebbb72d057235ba15ad7a9ae68472f28c6d68b3ead860dfd8d4130839/68747470733a2f2f696d672e736869656c64732e696f2f636f6465636f762f632f6769746875622f746f65766572797468696e672f616666696e653f7374796c653d666c61742d737175617265)](https://affine-storybook.vercel.app/) |\n" +
                "| [@toeverything/y-indexeddb](https://github.com/toeverything/AFFiNE/blob/canary/packages/common/y-indexeddb) | IndexedDB database adapter for Yjs | [![](https://camo.githubusercontent.com/2a7fb8989b84e1c55cf9b89d1db012b9acf5156f81c4964ddcf4a4e615afde7f/68747470733a2f2f696d672e736869656c64732e696f2f6e706d2f646d2f40746f65766572797468696e672f792d696e646578656464623f7374796c653d666c61742d73717561726526636f6c6f723d656565)](https://www.npmjs.com/package/@toeverything/y-indexeddb) |\n" +
                "| [@toeverything/theme](https://github.com/toeverything/AFFiNE/blob/canary/packages/common/theme) | AFFiNE theme | [![](https://camo.githubusercontent.com/fa6ca9ffa9b33f1b1c84b137f0fc66e9f8facac29f88db2c3c6a758e18d0708e/68747470733a2f2f696d672e736869656c64732e696f2f6e706d2f646d2f40746f65766572797468696e672f7468656d653f7374796c653d666c61742d73717561726526636f6c6f723d656565)](https://www.npmjs.com/package/@toeverything/theme) |\n";

        translate(text);
    }

    public static String translate(String text) {
        String url = "http://localhost:11434/api/chat";
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.set("role", "user");
//        message.set("content", "翻译下面内容，要求通俗易懂\n"+t);
//        message.set("content", "我希望你能充当英语翻译、希望你是翻译我给你的文本，然后跳过特殊符合和网址，不要写解释。如果只有特殊符合或者网址，" +
//                "下面是翻译内容： \n" + text);
        message.set("content", "请直接翻译以下文本中的英文部分，保留所有原始格式和标记不变。不要添加任何解释或上下文分析，只进行字面上的翻译。对于专有名词、术语或不常见的词汇，请保持原文不变。" +
                "确保翻译后的输出保持与输入相同的格式。翻译内容如下：\n"+ text);
        messages.add(message);

        // 创建请求体JSON对象
        JSONObject requestBody = new JSONObject();
//        requestBody.set("model", "gemma:7b-instruct-fp16");
        requestBody.set("model", "qwen:14b");
        requestBody.set("messages", messages);
        requestBody.set("stream", false);

        // 发送POST请求
        HttpResponse response = HttpRequest.post(url)
                .body(requestBody.toString())
                .execute();

        // 检查响应状态并打印结果
        if (response.isOk()) {
            String responseBody = response.body();
            JSONObject responseJson = new JSONObject(responseBody);
            String str = responseJson.getJSONObject("message").getStr("content");
            System.out.println(str);
            return str;
        } else {
            System.out.println("Request failed with status code: " + response.getStatus());
            throw new RuntimeException("翻译错误");
        }

    }

}
