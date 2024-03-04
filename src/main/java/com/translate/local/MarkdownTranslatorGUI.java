package com.translate.local;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class MarkdownTranslatorGUI extends JFrame {
    private final JFXPanel markdownPanel; // 用于显示原始Markdown渲染结果
    private final JFXPanel translatedPanel; // 用于显示翻译后的Markdown渲染结果
    private WebView markdownView;
    private WebView translatedView;
    private JButton openFileButton;
    private JButton translateButton;
    private JButton saveButton;
    private JLabel statusLabel;
    private String currentMarkdownContent; // 当前Markdown内容

    public MarkdownTranslatorGUI() {
        super("Markdown Translator");

        markdownPanel = new JFXPanel();
        translatedPanel = new JFXPanel();
        Platform.runLater(() -> {
            createMarkdownView();
            createTranslatedView();
        });

        openFileButton = new JButton("打开文件");
        openFileButton.addActionListener(this::openFile);

        translateButton = new JButton("开始翻译");
        translateButton.addActionListener(this::translateMarkdown);

        saveButton = new JButton("保存");
        saveButton.addActionListener(this::saveTranslatedMarkdown);

        statusLabel = new JLabel("Status: 准备");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, markdownPanel, translatedPanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.5);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openFileButton);
        buttonPanel.add(translateButton);
        buttonPanel.add(saveButton);

        Container contentPane = getContentPane();
        contentPane.add(splitPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        contentPane.add(statusLabel, BorderLayout.NORTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
    }

    private void createMarkdownView() {
        markdownView = new WebView();
        markdownView.getEngine().load("file:///D:/project/translate/src/main/resources/index.html");
        markdownPanel.setScene(new Scene(markdownView));
        markdownView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
            }
        });
    }

    private void createTranslatedView() {
        translatedView = new WebView();
        translatedView.getEngine().load("file:///D:/project/translate/src/main/resources/index.html");
        translatedPanel.setScene(new Scene(translatedView));
        translatedView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
            }
        });
    }


    private void openFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                currentMarkdownContent = new String(Files.readAllBytes(selectedFile.toPath()), StandardCharsets.UTF_8);
                renderMarkdown(markdownView, currentMarkdownContent); // 渲染原始Markdown内容
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "File could not be read", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void renderMarkdown(WebView webView, String markdownContent) {
        Platform.runLater(() -> {
            try {
                webView.getEngine().executeScript("renderMarkdown(`" + escapeJavaScriptString(markdownContent) + "`);");
            } catch (Exception e) {
                // 在Swing线程中显示错误对话框
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Markdown rendering failed.", "Error", JOptionPane.ERROR_MESSAGE));
            }
        });
    }


    private boolean isTranslated = false;
    private volatile String translatedContent = "";

    @SneakyThrows
    private void translateMarkdown(ActionEvent e) {
        final JDialog dialog = new JDialog();
        dialog.setTitle("Please Wait...");
        dialog.setLayout(new BorderLayout());
        dialog.add(new JLabel("正在翻译中..."), BorderLayout.CENTER);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(200, 100);
        dialog.setLocationRelativeTo(this);
        dialog.setModal(true);

        Thread thread = new Thread(() -> {
            try {
                // 模拟长时间运行的任务
                translatedContent = new Markdown().start(currentMarkdownContent);

                // 确保渲染和更新UI的代码在JavaFX线程中执行
                Platform.runLater(() -> {
                    renderMarkdown(translatedView, translatedContent); // 渲染翻译后的Markdown内容
                    isTranslated = true;
                });
            } finally {
                // 关闭等待对话框
                dialog.dispose();
            }
        });

        thread.start(); // 启动线程
        dialog.setVisible(true); // 显示等待对话框
    }


    private void saveTranslatedMarkdown(ActionEvent e) {
        if (!isTranslated) {
            JOptionPane.showMessageDialog(this, "Please translate the Markdown before saving.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(translatedContent); // 假设翻译内容是原内容转大写
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "File could not be saved", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String escapeJavaScriptString(String string) {
        return string.replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("$", "\\$");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MarkdownTranslatorGUI().setVisible(true));
    }
}
