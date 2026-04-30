package com.example.es1.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchHitVO {

    private String docId;
    private String fileName;
    private String fileType;
    private String filePath;
    private Long fileSize;
    private String category;
    private String region;
    private String tags;
    private LocalDateTime uploadTime;
    private Integer viewCount;
    private KeywordHighlightVO fileNameHighlight;
    private KeywordHighlightVO contentPreviewHighlight;
    private KeywordHighlightVO fullContentHighlight;
}
