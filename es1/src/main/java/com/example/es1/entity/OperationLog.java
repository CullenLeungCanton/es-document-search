package com.example.es1.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "operation_logs")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(length = 50)
    private String username;

    @Column(nullable = false, length = 30)
    private String operation;

    @Column(name = "doc_id", length = 50)
    private String docId;

    @Column(length = 200)
    private String keyword;

    @Column(name = "request_url", length = 500)
    private String requestUrl;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column
    private Integer status = 1;

    @Column(name = "error_msg", length = 500)
    private String errorMsg;

    @Column(name = "operation_time")
    private LocalDateTime operationTime;
}
