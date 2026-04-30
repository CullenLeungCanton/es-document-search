package com.example.es1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文档响应信息")
public class DocumentVO {

    private Integer id;
    private String docId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private String highlight;
    private String category;
    private String region;
    private String tags;
    private Integer viewCount;
    private Integer downloadCount;
    private LocalDateTime uploadTime;
    private String uploadUser;
}
