package com.example.es1.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.es1.common.exception.BusinessException;
import com.example.es1.common.result.PageResult;
import com.example.es1.common.utils.HighlightUtil;
import com.example.es1.common.utils.JwtUtil;
import com.example.es1.dto.*;
import com.example.es1.entity.nfDocument;
import com.example.es1.repository.es.DocumentEsRepository;
import com.example.es1.repository.jpa.DocumentJpaRepository;
import com.example.es1.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentJpaRepository documentJpaRepository;
    private final DocumentEsRepository documentEsRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final TextExtractService textExtractService;
    private final HighlightUtil highlightUtil;
    private final JwtUtil jwtUtil;
    private final ElasticsearchOperations elasticsearchOperations;

    @Value("${file.storage.base-path}")
    private String basePath;

    @Override
    public PageResult<SearchHitVO> search(DocumentSearchDTO dto) {
        String keyword = dto.getKeyword();
        String category = dto.getCategory();
        String region = dto.getRegion();
        int pageNum = dto.getPageNum() - 1;
        int pageSize = dto.getPageSize();

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        if (StrUtil.isNotBlank(keyword)) {
            Query searchQuery = Query.of(q -> q.multiMatch(m -> m.fields("fileName", "content").query(keyword)));
            boolBuilder.must(searchQuery);
        } else {
            boolBuilder.must(Query.of(q -> q.matchAll(m -> m)));
        }

        if (StrUtil.isNotBlank(category)) {
            boolBuilder.filter(Query.of(q -> q.term(t -> t.field("category").value(category))));
        }

        if (StrUtil.isNotBlank(region)) {
            boolBuilder.filter(Query.of(q -> q.term(t -> t.field("region").value(region))));
        }

        Highlight hl = new Highlight(List.of(new HighlightField("content", HighlightFieldParameters.builder().withPreTags("<em class='highlight'>").withPostTags("</em>").withFragmentSize(150).withNumberOfFragments(2).build())));

        HighlightQuery highlightQuery = new HighlightQuery(hl, null);

        NativeQuery nativeQuery = NativeQuery.builder().withQuery(boolBuilder.build()._toQuery()).withPageable(PageRequest.of(pageNum, pageSize)).withHighlightQuery(highlightQuery).build();

        SearchHits<nfDocument> searchHits = elasticsearchOperations.search(nativeQuery, nfDocument.class);

        long total = searchHits.getTotalHits();

        List<SearchHitVO> records = new ArrayList<>();
        for (SearchHit<nfDocument> hit : searchHits) {
            nfDocument doc = hit.getContent();
            SearchHitVO hitVO = new SearchHitVO();
            BeanUtil.copyProperties(doc, hitVO);

            documentJpaRepository.findByDocId(doc.getDocId()).ifPresent(dbDoc -> {
                hitVO.setFilePath(dbDoc.getFilePath());
                hitVO.setFileSize(dbDoc.getFileSize());
                hitVO.setViewCount(dbDoc.getViewCount());
            });

            if (StrUtil.isNotBlank(keyword)) {
                KeywordHighlightVO fileNameHighlight = highlightUtil.findKeywordPosition(doc.getFileName(), keyword, "fileName");
                hitVO.setFileNameHighlight(fileNameHighlight);

                KeywordHighlightVO contentPreview = highlightUtil.generatePreviewWithPosition(doc.getContent(), keyword, "content", 300);
                hitVO.setContentPreviewHighlight(contentPreview);
            } else {
                if (StrUtil.isNotBlank(doc.getContent())) {
                    String preview = doc.getContent().length() > 200 ? doc.getContent().substring(0, 200) + "..." : doc.getContent();
                    hitVO.setContentPreviewHighlight(highlightUtil.generatePreviewWithPosition(preview, keyword, "content", 300));
                }
            }

            records.add(hitVO);
        }

        return PageResult.of(total, records, dto.getPageNum(), pageSize);
    }

    @Override
    @Transactional
    public String upload(DocumentUploadDTO dto) {
        MultipartFile file = dto.getFile();
        String originalFilename = file.getOriginalFilename();

        try {

            byte[] fileBytes = file.getBytes();
            String docId = IdUtil.fastSimpleUUID();
            String content = textExtractService.extractFromBytes(fileBytes, originalFilename);
            String filePath = saveFileToDisk(fileBytes, originalFilename, docId);

            nfDocument nfDocument = new nfDocument();
            nfDocument.setDocId(docId);
            nfDocument.setFileName(file.getOriginalFilename());
            nfDocument.setFileType(getFileType(originalFilename));
            nfDocument.setFileSize(file.getSize());
            nfDocument.setFilePath(filePath);
            nfDocument.setCategory(dto.getCategory());
            nfDocument.setRegion(dto.getRegion());
            nfDocument.setTags(dto.getTags());
            nfDocument.setUploadTime(LocalDateTime.now());
            nfDocument.setUploadUser(dto.getUploadUser());
            nfDocument.setContent(content);
            nfDocument.setViewCount(0);
            nfDocument.setDownloadCount(0);

            documentJpaRepository.save(nfDocument);

            documentEsRepository.save(nfDocument);

            log.info("文档上传成功：docId={}, fileName={}", docId, file.getOriginalFilename());
            return docId;
        } catch (Exception e) {
            log.error("文档上传失败：{}", file.getOriginalFilename(), e);
            throw new BusinessException("文档上传失败：" + e.getMessage());
        }
    }

    @Override
    public nfDocument getDetail(String docId) {
        nfDocument nfDocument = documentJpaRepository.findByDocId(docId).orElseThrow(() -> new BusinessException("文档不存在"));

        nfDocument.setViewCount(nfDocument.getViewCount() + 1);
        documentJpaRepository.save(nfDocument);

        return nfDocument;
    }

    @Override
    @Transactional
    public void delete(String docId) {
        nfDocument nfDocument = documentJpaRepository.findByDocId(docId).orElseThrow(() -> new BusinessException("文档不存在"));

        File file = new File(nfDocument.getFilePath());
        if (file.exists()) {
            file.delete();
        }

        documentJpaRepository.deleteByDocId(docId);
        documentEsRepository.deleteByDocId(docId);

        log.info("文档删除成功：docId={}", docId);
    }

    private String saveFileToDisk(byte[] fileBytes, String fileName, String docId) {
        try {
            String dateDir = DateUtil.format(LocalDateTime.now(), "yyyy/MM");
            String dirPath = basePath + "/" + dateDir;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String safeFileName = timestamp + "_" + docId + "_" + fileName;
            String filePath = dirPath + "/" + safeFileName;
            java.nio.file.Files.write(new File(filePath).toPath(), fileBytes);

            return filePath;
        } catch (Exception e) {
            log.error("保存文件到磁盘失败", e);
            throw new BusinessException("保存文件失败：" + e.getMessage());
        }
    }

    private String removeHighlightTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<em class='highlight'>", "").replaceAll("</em>", "");
    }

    private String truncateChars(String text, int maxLength) {
        if (text == null || text.isEmpty()) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    private String getFileType(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (ext) {
            case "pdf": return "pdf";
            case "doc": return "word";
            case "docx": return "word";
            case "xls": return "excel";
            case "xlsx": return "excel";
            default: return "other";
        }
    }
}
