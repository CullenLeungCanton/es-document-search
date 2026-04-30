package com.example.es1.controller;

import com.example.es1.common.annotation.Log;
import com.example.es1.common.result.PageResult;
import com.example.es1.common.result.Result;
import com.example.es1.dto.DocumentSearchDTO;
import com.example.es1.dto.DocumentUploadDTO;
import com.example.es1.dto.SearchHitVO;
import com.example.es1.entity.nfDocument;
import com.example.es1.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文档管理",description = "文档上传、搜索、详情接口")
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "上传文档")
    @Log(operation = "upload")
    @PostMapping("/upload")
    public Result<String> upload(@RequestPart(value = "file", required = false) MultipartFile file, @RequestParam(required = false) String category, @RequestParam(required = false) String region, @RequestParam(required = false) String tags, @RequestParam(required = false) String uploadUser, HttpServletRequest request) {
        DocumentUploadDTO dto = new DocumentUploadDTO();
        dto.setFile(file);
        dto.setCategory(category);
        dto.setRegion(region);
        dto.setTags(tags);
        dto.setUploadUser(uploadUser);
        String docId = documentService.upload(dto);
        return Result.success(docId);
    }

    @Operation(summary = "搜索文档")
    @Log(operation = "search", keywordParam = "keyword")
    @PostMapping("/search")
    public Result<PageResult<SearchHitVO>> search(@RequestBody DocumentSearchDTO dto) {
        PageResult<SearchHitVO> result = documentService.search(dto);
        return Result.success(result);
    }

    @Operation(summary = "获取文档详情")
    @Log(operation = "view", docIdParam = "docId")
    @GetMapping("/detail/{docId}")
    public Result<nfDocument> detail(@PathVariable String docId) {
        nfDocument nfDocument = documentService.getDetail(docId);
        return Result.success(nfDocument);
    }

    @Operation(summary = "删除文档")
    @Log(operation = "delete", docIdParam = "docId")
    @DeleteMapping("/{docId}")
    public Result<Void> delete(@PathVariable String docId) {
        documentService.delete(docId);
        return Result.success();
    }
}
