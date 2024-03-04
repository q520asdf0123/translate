package com.translate.local;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

public class ApiGenerateRequest {

    private static final String url = "http://localhost:11434/api/generate";
    ;

    public static void main(String[] args) {
        String text = "| Name |  |  |\n" +
                "| --- | --- | --- |\n" +
                "| [@affine/component](https://github.com/toeverything/AFFiNE/blob/canary/packages/frontend/component) | AFFiNE Component Resources | [![](https://camo.githubusercontent.com/c2d01cfebbb72d057235ba15ad7a9ae68472f28c6d68b3ead860dfd8d4130839/68747470733a2f2f696d672e736869656c64732e696f2f636f6465636f762f632f6769746875622f746f65766572797468696e672f616666696e653f7374796c653d666c61742d737175617265)](https://affine-storybook.vercel.app/) |\n" +
                "| [@toeverything/y-indexeddb](https://github.com/toeverything/AFFiNE/blob/canary/packages/common/y-indexeddb) | IndexedDB database adapter for Yjs | [![](https://camo.githubusercontent.com/2a7fb8989b84e1c55cf9b89d1db012b9acf5156f81c4964ddcf4a4e615afde7f/68747470733a2f2f696d672e736869656c64732e696f2f6e706d2f646d2f40746f65766572797468696e672f792d696e646578656464623f7374796c653d666c61742d73717561726526636f6c6f723d656565)](https://www.npmjs.com/package/@toeverything/y-indexeddb) |\n" +
                "| [@toeverything/theme](https://github.com/toeverything/AFFiNE/blob/canary/packages/common/theme) | AFFiNE theme | [![](https://camo.githubusercontent.com/fa6ca9ffa9b33f1b1c84b137f0fc66e9f8facac29f88db2c3c6a758e18d0708e/68747470733a2f2f696d672e736869656c64732e696f2f6e706d2f646d2f40746f65766572797468696e672f7468656d653f7374796c653d666c61742d73717561726526636f6c6f723d656565)](https://www.npmjs.com/package/@toeverything/theme) |\n";

    }

    public static String translate(String text) {
        JSONObject json = new JSONObject();
//        json.set("model", "gemma:7b-instruct-fp16");
        json.set("model", "qwen:14b");
//        json.set("prompt", "请直接翻译以下文本，不要添加任何解释或上下文分析。即使翻译结果可能看起来不完整或缺乏上下文，也请只提供字面上的翻译。对于专有名词、术语或不常见的词汇，请保持原文不变，" +
//                "直接翻译其他部分。翻译内容如下：\n"+ text);
        json.set("prompt", "请直接翻译以下文本中的英文部分，保留所有原始格式和标记不变。不要添加任何解释或上下文分析，只进行字面上的翻译。对于专有名词、术语或不常见的词汇，请保持原文不变。" +
                "确保翻译后的输出保持与输入相同的格式。翻译内容如下：\n" + text);
        json.set("stream", false);
        // 发送POST请求
        HttpResponse response = HttpRequest.post(url)
                .body(json.toString())
                .execute();

        // 检查响应状态并打印结果
        if (response.isOk()) {
            String responseBody = response.body();
            JSONObject responseJson = new JSONObject(responseBody);
            String str = responseJson.getStr("content");
            System.out.println(str);
            return str;
        } else {
            System.out.println("Request failed with status code: " + response.getStatus());
            throw new RuntimeException("翻译错误");
        }
    }
}
