package com.example.es1.service;

import com.example.es1.common.result.PageResult;
import com.example.es1.dto.OperationLogQueryDTO;
import com.example.es1.dto.OperationLogVO;
import com.example.es1.dto.OperationStatisticsDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface OperationLogService {

    void log(String operation, String docId, String keyword, HttpServletRequest request, Long durationMs, Integer status, String errorMsg);

    void log(String operation, String docId, String keyword, HttpServletRequest request, Integer status, String errorMsg);

    void log(String operation, HttpServletRequest request);

    PageResult<OperationLogVO> queryLogs(OperationLogQueryDTO queryDTO);

    OperationLogVO getLogDetail(Long id);

    OperationStatisticsDTO getStatistics(LocalDateTime startTime, LocalDateTime endTime);

    List<OperationLogVO> getRecentLogs(Integer limit, Integer userId);

    List<OperationLogVO> getDocLogs(String docId);

    void cleanOldLogs(int days);
}
