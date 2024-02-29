package com.translate;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class MarkdownTranslator {

    private static final Pattern URL_PATTERN = Pattern.compile("http[s]?://\\S+");

    public static void main(String[] args) {
        String inputFilePath = "D:\\make.md";
        String outputFilePath = "D:\\output.md";
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
            String line;
            StringBuilder markdownParagraph = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (URL_PATTERN.matcher(line).matches()) {
                    // Write URLs directly to output without translating
                    writer.write(line);
                    writer.newLine();
                } else if (!line.trim().isEmpty()) {
                    markdownParagraph.append(line).append("\n");
                } else {
                    // Process the collected paragraph
                    String collectedParagraph = markdownParagraph.toString();
                    markdownParagraph.setLength(0); // Reset for the next paragraph
                    if (!collectedParagraph.isEmpty()) {
                        future = future.thenCompose(v -> translateAndWrite(collectedParagraph, writer));
                    }
                }
            }

            // Process the last paragraph if exists
            if (markdownParagraph.length() > 0) {
                future = future.thenCompose(v -> translateAndWrite(markdownParagraph.toString(), writer));
            }

            // Wait for all futures to complete
            future.get();

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("Error processing the Markdown file.");
        }
    }

    private static CompletableFuture<Void> translateAndWrite(String markdownParagraph, BufferedWriter writer) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        return translateWithOpenAI(markdownParagraph);
                    } catch (NoApiKeyException e) {
                        throw new RuntimeException(e);
                    } catch (InputRequiredException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(translation -> {
                    try {
                        writer.write(markdownParagraph); // Write the original paragraph
                        writer.newLine();
                        writer.write(translation); // Write the translated paragraph
                        writer.newLine();
                        writer.newLine(); // Add an extra line to separate paragraphs
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    // 使用OpenAI API进行翻译的示例方法（需要实现）
    private static String translateWithOpenAI(String text) throws NoApiKeyException, InputRequiredException {
        // 这里应该是调用OpenAI API进行翻译的代码
        // 由于实际的API调用依赖于具体的API接口和认证，这里不提供具体实现
        // 返回翻译后的文本作为示例
        if(!StringUtils.hasText(text)){
            return "";
        }

        Generation gen = new Generation();
        MessageManager msgManager = new MessageManager(10);
        Message systemMsg =
                Message.builder().role(Role.SYSTEM.getValue()).content("翻译下面语言为中文,跳过网址").build();
        Message userMsg = Message.builder().role(Role.USER.getValue()).content(text).build();
        msgManager.add(systemMsg);
        msgManager.add(userMsg);
        QwenParam param = QwenParam.builder().model("qwen-max-1201").messages(msgManager.get())
                .apiKey("sk-ac7ea97ecadb4f6f9830a6b43cfeeb8a").resultFormat(QwenParam.ResultFormat.MESSAGE).build();
        GenerationResult result = gen.call(param);
        String content = result.getOutput().getChoices().get(0).getMessage().getContent();
        System.out.println(content);
        return "翻译内容: " + content;
    }
}