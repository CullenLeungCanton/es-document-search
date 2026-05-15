package com.example.es1.controller;

import cn.hutool.core.io.FileUtil;
import com.example.es1.common.annotation.Log;
import com.example.es1.common.exception.BusinessException;
import com.example.es1.entity.Document;
import com.example.es1.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@Tag(name = "文档文件", description = "文档预览和下载")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final DocumentService documentService;

    @Operation(summary = "预览文件（在线查看）")
    @Log(operation = "preview", docIdParam = "docId")
    @GetMapping("/preview/{docId}")
    public ResponseEntity<Resource> preview(@PathVariable String docId, HttpServletRequest request) {
        Document Document = documentService.getDetail(docId, request);
        String filePath = Document.getFilePath();
        String fileName = Document.getFileName();

        File file = new File(filePath);
        if (!file.exists()) {
            throw new BusinessException("文件不存在");
        }

        Resource resource = new FileSystemResource(file);
        String contentType = getContentType(fileName);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"").body(resource);
    }

    @Operation(summary = "下载文件")
    @Log(operation = "download", docIdParam = "docId")
    @GetMapping("/download/{docId}")
    public ResponseEntity<Resource> download(@PathVariable String docId, HttpServletRequest request) {
        Document Document = documentService.getDetail(docId, request);
        String filePath = Document.getFilePath();
        String fileName = Document.getFileName();

        File file = new File(filePath);
        if (!file.exists()) {
            throw new BusinessException("文件不存在");
        }

        Resource resource = new FileSystemResource(file);
        String contentType = getContentType(fileName);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").body(resource);
    }

    private String getContentType(String fileName) {
        String ext = FileUtil.getSuffix(fileName).toLowerCase();
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "doc" -> "application/msword";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xls" -> "application/vnd.ms-excel";
            default -> "application/octet-stream";
        };
    }
}
