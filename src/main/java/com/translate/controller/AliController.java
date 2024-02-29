package com.translate.controller;

import com.vladsch.flexmark.html.HtmlRenderer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class AliController {


    @PostConstruct
    public void test(){

    }

    public static void main(String[] args) {
        // 创建Markdown解析器和HTML渲染器实例
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        // Markdown文件路径
        String filePath = "D:\\Untitled-3.md";

        try {
            // 读取文件内容为字符串
            String markdown = new String(Files.readAllBytes(Paths.get(filePath)));

            // 解析Markdown文本为AST
            Node document = parser.parse(markdown);

            // 将AST渲染为HTML
            String html = renderer.render(document);

            // 输出HTML
            System.out.println(html);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading Markdown file.");
        }
    }

}
