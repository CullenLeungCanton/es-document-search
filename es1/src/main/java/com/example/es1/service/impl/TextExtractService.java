package com.example.es1.service.impl;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Service
public class TextExtractService {

    public String extractFromBytes(byte[] fileBytes, String fileName) {
        if (fileBytes == null || fileBytes.length == 0) {
            log.warn("文件名为空");
            return "";
        }
        String ext = FileUtil.getSuffix(fileName).toLowerCase();
        log.info("开始提取文本：filename={}, ext={}, size={}", fileName, ext, fileBytes.length);

        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            String result;
            switch (ext) {
                case "pdf":
                    result = extractFromPdf(inputStream);
                    break;
                case "docx":
                    result = extractFromDocx(inputStream);
                    break;
                case "doc":
                    result = extractFromDoc(inputStream);
                    break;
                case "xlsx":
                    result = extractFromExcel(inputStream, "xlsx");
                    break;
                case "xls":
                    result = extractFromExcel(inputStream, "xls");
                    break;
                default:
                    log.warn("不支持的文件类型：{}", ext);
                    result = "";
            }
            log.info("文本提取成功：filename={}, 文本长度={}", fileName, result.length());
            return result;
        } catch (Exception e) {
            log.error("文本提取失败：filename={}, 错误={}", fileName, e.getMessage(), e);
            return "";
        }
    }

    private String extractFromPdf(InputStream inputStream) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            String text = stripper.getText(document);
            return cleanText(text);
        }
    }

    private String extractFromDocx(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isEmpty()) {
                    sb.append(text).append("\n");
                }
            }

            document.getTables().forEach(table -> {
                table.getRows().forEach(row -> {
                    row.getTableCells().forEach(cell -> {
                        String text = cell.getText();
                        if (text != null && !text.isEmpty()) {
                            sb.append(text).append("\t");
                        }
                    });
                });
            });
            log.debug("DOCX提取文本长度：{}", sb.length());
            return cleanText(sb.toString());
        } catch (Exception e) {
            log.error("文本提取失败：{}", e.getMessage());
            return "";
        }
    }

    private String extractFromDoc(InputStream inputStream) throws Exception {
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            WordExtractor extractor = new WordExtractor(document);
            String text = extractor.getText();
            return cleanText(text);
        } catch (Exception e) {
            log.error("文本提取失败：{}", e.getMessage());
            return "";
        }
    }

    private String extractFromExcel(InputStream inputStream, String ext) throws Exception {
        Workbook workbook;
        if ("xlsx".equals(ext)) {
            workbook = new XSSFWorkbook(inputStream);
        } else {
            workbook = new HSSFWorkbook(inputStream);
        }

        try (workbook) {
            StringBuilder sb = new StringBuilder();
            int sheetCount = workbook.getNumberOfSheets();
            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                sb.append("【工作表：").append(sheetName).append("】\n");
                for (Row row : sheet) {
                    if (row == null) continue;
                    for (Cell cell : row) {
                        String cellValue = getCellValue(cell);
                        if (cellValue != null && !cellValue.isEmpty()) {
                            sb.append(cellValue).append("\t");
                        }
                    }
                    sb.append("\n");
                }
                sb.append("\n");
            }
            return cleanText(sb.toString());
        } catch (Exception e) {
            log.error("文本提取失败：{}", e.getMessage());
            return "";
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }

                double numValue = cell.getNumericCellValue();
                if (numValue == (long) numValue) {
                    yield String.valueOf(numValue);
                }
                yield String.valueOf(numValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BLANK -> "";
            case ERROR -> "错误：" + cell.getErrorCellValue();
            default -> "";
        };
    }

    private String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String cleaned = text.replaceAll("\\s+", " ");
        cleaned = cleaned.trim();
        return cleaned;
    }
}
