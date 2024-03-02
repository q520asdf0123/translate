//package com.hero.log.action;
//
//import com.alibaba.dashscope.aigc.generation.Generation;
//import com.alibaba.dashscope.aigc.generation.GenerationResult;
//import com.alibaba.dashscope.aigc.generation.models.QwenParam;
//import com.alibaba.dashscope.common.Message;
//import com.alibaba.dashscope.common.MessageManager;
//import com.alibaba.dashscope.common.Role;
//import com.alibaba.dashscope.exception.InputRequiredException;
//import com.alibaba.dashscope.exception.NoApiKeyException;
//import org.springframework.util.StringUtils;
//
//import java.io.*;
//import java.util.*;
//import java.util.regex.*;
//
//public class MarkdownTranslator {
//
//    private static final Pattern NON_TEXT_PATTERN = Pattern.compile("(^\\[|^\\!\\[|\\]\\(.*?\\)$|^#+\\s+.*)");
//
//    public static void main(String[] args) {
//        String inputFilePath = "D:\\project\\m1.md"; // 替换成您的Markdown文件路径
//        String outputFilePath = "D:\\project\\translated_markdown.md"; // 替换成输出文件的路径
//        translateMarkdownFile(inputFilePath, outputFilePath);
//    }
//
//    private static void translateMarkdownFile(String inputPath, String outputPath) {
//        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
//             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
//            String line;
//            List<String> segmentLines = new ArrayList<>();
//
//            while ((line = reader.readLine()) != null) {
//                if (NON_TEXT_PATTERN.matcher(line).matches()) {
//                    // If the line is a non-text element, translate and write the accumulated segment first
//                    if (!segmentLines.isEmpty()) {
//                        String segment = String.join(System.lineSeparator(), segmentLines);
//                        writer.write(segment);
//                        writer.newLine();
//                        writer.write(translate(segment)); // Translate the segment
//                        writer.newLine();
//                        segmentLines.clear(); // Clear the accumulated lines
//                    }
//                    writer.write(line); // Write the non-text line
//                    writer.newLine();
//                } else {
//                    segmentLines.add(line); // Accumulate lines for text segments
//                }
//            }
//            // Don't forget to process the last segment
//            if (!segmentLines.isEmpty()) {
//                String segment = String.join(System.lineSeparator(), segmentLines);
//                writer.write(segment);
//                writer.newLine();
//                writer.write(translate(segment)); // Translate the segment
//                writer.newLine();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NoApiKeyException e) {
//            throw new RuntimeException(e);
//        } catch (InputRequiredException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    private static String translate(String text) throws NoApiKeyException, InputRequiredException {
//        // 这里应该是调用OpenAI API进行翻译的代码
//        // 由于实际的API调用依赖于具体的API接口和认证，这里不提供具体实现
//        // 返回翻译后的文本作为示例
//        if (!StringUtils.hasText(text)) {
//            return "";
//        }
//
//        Generation gen = new Generation();
//        MessageManager msgManager = new MessageManager(10);
//        Message systemMsg =
//                Message.builder().role(Role.SYSTEM.getValue()).content("翻译下面语言为中文,输入什么格式，就输出什么格式，然后跳过网址,").build();
//        Message userMsg = Message.builder().role(Role.USER.getValue()).content(text).build();
//        msgManager.add(systemMsg);
//        msgManager.add(userMsg);
//        QwenParam param = QwenParam.builder().model("qwen-max").messages(msgManager.get())
//                .apiKey("sk-ac7ea97ecadb4f6f9830a6b43cfeeb8a").resultFormat(QwenParam.ResultFormat.MESSAGE).build();
//        GenerationResult result = gen.call(param);
//        String content = result.getOutput().getChoices().get(0).getMessage().getContent();
//        System.out.println(content);
//        return "Translated: " + content;
//    }
//}