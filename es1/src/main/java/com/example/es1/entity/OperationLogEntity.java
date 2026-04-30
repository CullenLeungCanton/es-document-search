package com.example.es1.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogEntity {

    private Long id;
    private Integer userId;
    private String username;
    private String operation;
    private String docId;
    private String keyword;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String ipAddress;
    private String userAgent;
    private Long durationMs;
    private Integer status;
    private  String errorMsg;
    private LocalDateTime operationTime;
}
