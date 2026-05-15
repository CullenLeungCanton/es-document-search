package com.example.es1.controller;

import com.example.es1.common.annotation.Log;
import com.example.es1.common.result.PageResult;
import com.example.es1.common.result.Result;
import com.example.es1.dto.DocumentSearchDTO;
import com.example.es1.dto.DocumentUploadDTO;
import com.example.es1.dto.SearchHitVO;
import com.example.es1.entity.Document;
import com.example.es1.repository.jpa.DocumentJpaRepository;
import com.example.es1.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "文档管理",description = "文档上传、搜索、详情接口")
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentJpaRepository documentJpaRepository;

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
    public Result<Document> detail(@PathVariable String docId, HttpServletRequest request) {
        Document Document = documentService.getDetail(docId, request);
        return Result.success(Document);
    }

    @Operation(summary = "删除文档")
    @Log(operation = "delete", docIdParam = "docId")
    @DeleteMapping("/{docId}")
    public Result<Void> delete(@PathVariable String docId) {
        documentService.delete(docId);
        return Result.success();
    }

    @Operation(summary = "后台管理-获取所有文档列表")
    @GetMapping("/admin/documents")
    public Result<PageResult<Document>> listAllDocuments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "uploadTime"));
        Page<Document> page;

        Specification<Document> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(fileType)) {
                predicates.add(cb.equal(root.get("fileType"), fileType));
            }

            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.like(root.get("fileName"), "%" + keyword + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        page = documentJpaRepository.findAll(spec, pageable);

        return Result.success(PageResult.of(page.getTotalElements(), page.getContent(), pageNum, pageSize));
    }
}
