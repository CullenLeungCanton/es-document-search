package com.example.es1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "操作日志响应")
public class OperationLogVO {

    private Long id;
    private Integer userId;
    private String username;
    private String operation;
    private String docId;
    private String keyword;
    private String requestUrl;
    private String requestMethod;
    private String ipAddress;
    private String userAgent;
    private Long durationMs;
    private Integer status;
    private String errorMsg;
    private LocalDateTime operationTime;
}
