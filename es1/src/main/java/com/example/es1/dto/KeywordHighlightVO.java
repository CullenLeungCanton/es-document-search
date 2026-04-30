package com.example.es1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Schema(description = "关键词高亮位置信息")
public class KeywordHighlightVO {

    @Schema(description = "字段名")
    private String field;

    @Schema(description = "原始文本内容")
    private String text;

    @Schema(description = "关键词位置列表")
    private List<KeywordPosition> positions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "关键词文本")
    public static class KeywordPosition {
        @Schema(description = "关键词文本")
        private String keyword;

        @Schema(description = "起始位置")
        private Integer start;

        @Schema(description = "结束位置")
        private Integer end;
    }
}
