package org.interviewmate.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.interviewmate.service.FileParserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class FileParserServiceImpl implements FileParserService {

    @Override
    public String parseFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "";
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            filename = "";
        }

        String extension = getFileExtension(filename).toLowerCase();

        try {
            switch (extension) {
                case "pdf":
                    return parsePdf(file);
                case "doc":
                case "docx":
                    return parseWord(file);
                case "txt":
                    return parseTxt(file);
                default:
                    log.warn("不支持的文件格式: {}", extension);
                    // 尝试作为文本解析
                    return parseTxt(file);
            }
        } catch (Exception e) {
            log.error("解析文件失败: {}", e.getMessage());
            throw new RuntimeException("解析文件失败: " + e.getMessage());
        }
    }

    private String parsePdf(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    private String parseWord(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph para : paragraphs) {
                String text = para.getText();
                if (text != null && !text.trim().isEmpty()) {
                    sb.append(text).append("\n");
                }
            }
            return sb.toString();
        }
    }

    private String parseTxt(MultipartFile file) throws IOException {
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }
}
