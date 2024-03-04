package com.translate.controller;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TranslateController {

    @PostMapping("/translate")
    public JSONObject translate(@RequestBody JSONObject jsonObject) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        MessageManager msgManager = new MessageManager(10);
        Message systemMsg =
                Message.builder().role(Role.SYSTEM.getValue()).content("请直接翻译以下文本中的英文部分，保留所有原始格式和标记不变。不要添加任何解释或上下文分析，只进行字面上的翻译。对于专有名词、术语或不常见的词汇，请保持原文不变。\" +\n" +
                        "                \"确保翻译后的输出保持与输入相同的格式。翻译内容如下：").build();
        String text = jsonObject.getString("text_list");
        Message userMsg = Message.builder().role(Role.USER.getValue()).content(text).build();
        msgManager.add(systemMsg);
        msgManager.add(userMsg);
        QwenParam param = QwenParam.builder().model(Generation.Models.QWEN_TURBO).messages(msgManager.get())
                .apiKey("sk-ac7ea97ecadb4f6f9830a6b43cfeeb8a").resultFormat(QwenParam.ResultFormat.MESSAGE).build();
        GenerationResult result = gen.call(param);
        String content = result.getOutput().getChoices().get(0).getMessage().getContent();
        com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
        com.alibaba.fastjson.JSONObject r = new com.alibaba.fastjson.JSONObject();
        r.put("detected_source_lang", jsonObject.getString("source_lang"));
        r.put("text", content);
        object.put("translations", Lists.newArrayList(r));
        return object;
    }

}
