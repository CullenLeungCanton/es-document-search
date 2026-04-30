package com.example.es1.controller;

import com.example.es1.common.result.PageResult;
import com.example.es1.common.result.Result;
import com.example.es1.dto.OperationLogQueryDTO;
import com.example.es1.dto.OperationLogVO;
import com.example.es1.dto.OperationStatisticsDTO;
import com.example.es1.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Tag(name = "操作审计", description = "操作日志查询统计")
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class OperationLogController {
    private final OperationLogService operationLogService;

    @Operation(summary = "分页查询操作日志")
    @PostMapping("/logs")
    public Result<PageResult<OperationLogVO>> queryLogs(@Valid @RequestBody OperationLogQueryDTO queryDTO) {
        PageResult<OperationLogVO> result = operationLogService.queryLogs(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取日志详情")
    @GetMapping("/logs/{id}")
    public Result<OperationLogVO> getLogDetail(@PathVariable Long id) {
        OperationLogVO log = operationLogService.getLogDetail(id);
        return Result.success(log);
    }

    @Operation(summary = "获取文档操作记录")
    @GetMapping("/log/doc/{docId}")
    public Result<List<OperationLogVO>> getDocLogs(@PathVariable String docId) {
        List<OperationLogVO> logs = operationLogService.getDocLogs(docId);
        return Result.success(logs);
    }

    @Operation(summary = "操作统计分析")
    @GetMapping("/statistics")
    public Result<OperationStatisticsDTO> getStatistics(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
//        if (startTime == null) {
//            startTime = LocalDateTime.now().minusDays(7);
//        }
//        if (endTime == null) {
//            endTime = LocalDateTime.now();
//        }
        LocalDateTime startTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        OperationStatisticsDTO statistics = operationLogService.getStatistics(startTime, endTime);
        return Result.success(statistics);
    }

    @Operation(summary = "清理过期日志（管理员）")
    @DeleteMapping("/logs/clean")
    public Result<Void> cleanOldLogs(@RequestParam(defaultValue = "90") Integer days) {
        operationLogService.cleanOldLogs(days);
        return Result.success("已清理 " + days + " 天前的日志", null);
    }
}
