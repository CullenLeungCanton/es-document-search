package com.example.es1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Schema(description = "操作统计")
public class OperationStatisticsDTO {

    @Schema(description = "操作类型统计")
    private List<OperationStat> operationStats;

    @Schema(description = "每日统计")
    private List<DailyStat> dailyStats;

    @Schema(description = "活跃用户TOP10")
    private List<UserStat> topUsers;

    @Schema(description = "热门文档TOP10")
    private List<DocStat> hotDocs;

    @Schema(description = "总操作数")
    private Long totalCount;

    @Schema(description = "成功操作数")
    private Long successCount;

    @Schema(description = "失败操作数")
    private Long failCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "操作类型统计")
    public static class OperationStat {
        @Schema(description = "操作类型")
        private String operation;

        @Schema(description = "次数")
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "每日统计")
    public static class DailyStat {
        @Schema(description = "日期")
        private String date;

        @Schema(description = "操作次数")
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户统计")
    public static class UserStat {
        @Schema(description = "用户名")
        private String username;

        @Schema(description = "操作次数")
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "文档统计")
    public static class DocStat {
        @Schema(description = "文档ID")
        private String docId;

        @Schema(description = "文档名称")
        private String fileName;

        @Schema(description = "浏览次数")
        private Long viewCount;

        @Schema(description = "下载次数")
        private Long downloadCount;
    }
}
