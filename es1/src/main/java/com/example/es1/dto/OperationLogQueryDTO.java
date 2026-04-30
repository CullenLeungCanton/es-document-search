package com.example.es1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "操作日志查询参数")
public class OperationLogQueryDTO {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "操作类型")
    private String operation;

    @Schema(description = "文档ID")
    private String docId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态：1成功 0失败")
    private Integer status;

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", defaultValue = "20")
    private Integer pageSize = 20;
}
