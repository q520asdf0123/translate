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

    private static final Pattern URL_PATTERN = Pattern.compile("\\[([^]]+)\\]\\((http[^)]+)\\)");
    private static final String TRANSLATION_PREFIX = "翻译内容: ";


    public static void main(String[] args) {
        String inputFilePath = "D:\\make.md";
        String outputFilePath = "D:\\output.md";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
            String line;
            StringBuilder paragraph = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() ) {
                    // Accumulate paragraph text, skipping URLs
                    paragraph.append(line).append("\n");
                } else {
                    if (paragraph.length() > 0) {
                        // Paragraph ends here. Process the accumulated paragraph text.
                        String paragraphText = paragraph.toString();
                        future = future.thenCompose(v -> translateAndWrite(paragraphText, writer));
                        paragraph.setLength(0); // Reset the StringBuilder for the next paragraph
                    }
                    // Write the original line (empty line or line with URL) to maintain the structure
                    writer.write(line);
                    writer.newLine();
                }
            }

            // If there is any remaining paragraph text not followed by an empty line, process it.
            if (paragraph.length() > 0) {
                future = future.thenCompose(v -> translateAndWrite(paragraph.toString(), writer));
            }

            // Wait for all futures to complete
            future.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("Error processing the Markdown file.");
        }
    }


    private static CompletableFuture<Void> translateAndWrite(String markdownLine, BufferedWriter writer) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        return translateWithOpenAI(markdownLine);
                    } catch (NoApiKeyException | InputRequiredException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(translation -> {
                    try {
                        writer.write(markdownLine);
                        writer.newLine();
                        // Write the translation using Markdown quote syntax if it's not an empty line
                        if (!markdownLine.trim().isEmpty()) {
                            // 使用Markdown的引用语法来展示翻译内容
                            writer.write("> " + translation);
                            writer.newLine();
                        }
                        writer.newLine(); // Add an extra line to separate from the next content
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
                Message.builder().role(Role.SYSTEM.getValue()).content("翻译下面语言为中文,输入什么格式，就输出什么格式，然后跳过网址,").build();
        Message userMsg = Message.builder().role(Role.USER.getValue()).content(text).build();
        msgManager.add(systemMsg);
        msgManager.add(userMsg);
        QwenParam param = QwenParam.builder().model("qwen-plus").messages(msgManager.get())
                .apiKey("sk-ac7ea97ecadb4f6f9830a6b43cfeeb8a").resultFormat(QwenParam.ResultFormat.MESSAGE).build();
        GenerationResult result = gen.call(param);
        String content = result.getOutput().getChoices().get(0).getMessage().getContent();
        System.out.println(content);
        return  content;
    }
}