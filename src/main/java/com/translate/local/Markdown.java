package com.translate.local;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Markdown {
    private static final Pattern URL_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)");

    private static final String PLACEHOLDER = "_XKXK_%d";
    private static final Pattern NON_TEXT_LINE_PATTERN = Pattern.compile(
            "^(\\s*|\\p{Punct}+|\\[!?\\[[^\\]]*\\]\\([^)]+\\)\\s*)+$"
    );

    private static int placeholderCount = 0; // Make placeholder count static so it persists across method calls
    private final List<String> placeList = new ArrayList<>();


    public static void main(String[] args) throws NoApiKeyException, InputRequiredException {
        Markdown markdown = new Markdown();
        String inputFilePath = "D:\\make.md";
        String outputFilePath = "D:\\translated_make.md";

        StringBuilder contentBuilder = new StringBuilder();

        // 读取文件内容
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 翻译文件内容
        String translatedContent = markdown.start(contentBuilder.toString());

        // 将翻译后的内容写入新文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(translatedContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String start(String text) {
        try {
            Markdown markdown = new Markdown();

            MutableDataSet options = new MutableDataSet();
            options.set(Parser.EXTENSIONS, Arrays.asList(TocExtension.create(), TablesExtension.create()));

            Parser parser = Parser.builder(options).build();
            Document document = parser.parse(text);

            StringBuilder newMarkdown = new StringBuilder();
            List<String> urls = new ArrayList<>();

            for (Node node : document.getChildren()) {
                markdown.processNode(node, newMarkdown, urls);
            }

            // 将新生成的Markdown内容写入到输出文件
//            Files.write(Paths.get(outputFilePath), newMarkdown.toString().getBytes(), StandardOpenOption.CREATE);
            return newMarkdown.toString();
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (InputRequiredException e) {
            throw new RuntimeException(e);
        }
    }

    private void processNode(Node node, StringBuilder newMarkdown, List<String> urls) throws NoApiKeyException, InputRequiredException {
        BasedSequence chars = node.getChars();
        String originalText = chars.toString();

        boolean textIsEmpty = originalText.trim().isEmpty();

        //特殊印记
        String filteredText = originalText.replaceAll("\\[!?[^\\]]*\\]\\([^\\)]*\\)", "");

        // 去除网址
        String textWithoutUrls = originalText.replaceAll("(https?:\\/\\/[^\\s]+)", "");
        // 去除所有特殊字符和空白字符
        String textWithoutSpecialChars = textWithoutUrls.replaceAll("[\\p{P}\\p{S}\\s]", "");

        filteredText = filteredText.replaceAll("(https?:\\/\\/[^\\s]+)", "");
        filteredText = filteredText.replaceAll("[\\p{P}\\p{S}\\s]", "");

        if (filteredText.isEmpty()
                || textWithoutSpecialChars.isEmpty() || textIsEmpty
                || node instanceof FencedCodeBlock
                || node instanceof Link
                || node instanceof Image) {
            newMarkdown.append(originalText).append("\n\n");
        } else {
            // Process non-code blocks and non-link/image nodes
            String lineWithPlaceholders = replaceUrlsWithPlaceholders(originalText, urls);
            String translatedText = ApiChatRequest.translate(lineWithPlaceholders);
            String lineWithUrls = restorePlaceholdersWithUrls(translatedText, urls);
            newMarkdown.append(originalText).append("\n\n").append(lineWithUrls).append("\n");
            placeList.clear();
        }

//        else if (node.hasChildren() || !(node instanceof TableBlock)) {
//            // Recursively process each child node
//            for (Node child : node.getChildren()) {
//                processNode(child, newMarkdown, urls);
//            }
//        }
//        else {
//            // Process non-code blocks and non-link/image nodes
//            String lineWithPlaceholders = replaceUrlsWithPlaceholders(originalText, urls);
//            String translatedText = ApiChatRequest.translate(lineWithPlaceholders);
//            String lineWithUrls = restorePlaceholdersWithUrls(translatedText, urls);
//            newMarkdown.append(originalText).append("\n\n").append(lineWithUrls).append("\n\n");
//        }
    }

    private String replaceUrlsWithPlaceholders(String text, List<String> urls) {
        Matcher matcher = URL_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String placeholder = String.format(PLACEHOLDER, placeholderCount++);
            urls.add(matcher.group(0)); // Save the entire match
            matcher.appendReplacement(sb, Matcher.quoteReplacement(placeholder));
            placeList.add(placeholder);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String restorePlaceholdersWithUrls(String text, List<String> urls) {
        String result = text;

        for (String s : placeList) {
            String[] s1 = s.split("_");
            result = result.replace(s, urls.get(Integer.parseInt(s1[2])));
        }
//        for (int i = 0; i < urls.size(); i++) {
//            String format = String.format(PLACEHOLDER, i);
//            result = result.replace(format, urls.get(i));
//        }
        return result;
    }

    /**
     * 通义千问翻译
     *
     * @param text
     * @return
     */
    private String translate(String text) throws NoApiKeyException, InputRequiredException {
        // 这里应该是调用OpenAI API进行翻译的代码
        // 由于实际的API调用依赖于具体的API接口和认证，这里不提供具体实现
        // 返回翻译后的文本作为示例
        if (!StringUtils.hasText(text)) {
            return "";
        }

        Generation gen = new Generation();
        MessageManager msgManager = new MessageManager(10);
        Message systemMsg =
                Message.builder().role(Role.SYSTEM.getValue()).content("翻译下面语言为中文,输入什么格式，就输出什么格式，然后跳过网址,").build();
        Message userMsg = Message.builder().role(Role.USER.getValue()).content(text).build();
        msgManager.add(systemMsg);
        msgManager.add(userMsg);
        QwenParam param = QwenParam.builder().model("qwen-max-1201").messages(msgManager.get())
                .apiKey("sk-ac7ea97ecadb4f6f9830a6b43cfeeb8a").resultFormat(QwenParam.ResultFormat.MESSAGE).build();
        GenerationResult result = gen.call(param);
        String content = result.getOutput().getChoices().get(0).getMessage().getContent();
        System.out.println(content);
        return content;
//        return  "翻译===========>"+text;
    }

}
