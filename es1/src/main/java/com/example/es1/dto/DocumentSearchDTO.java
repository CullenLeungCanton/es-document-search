package com.example.es1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文档搜索参数")
public class DocumentSearchDTO {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "分类筛选")
    private String category;

    @Schema(description = "区域筛选")
    private String region;

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", defaultValue = "20")
    private Integer pageSize = 20;
}
