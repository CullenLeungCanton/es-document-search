package com.example.es1.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import co.elastic.clients.elasticsearch.nodes.Http;
import com.example.es1.common.exception.BusinessException;
import com.example.es1.common.result.PageResult;
import com.example.es1.dto.OperationLogQueryDTO;
import com.example.es1.dto.OperationLogVO;
import com.example.es1.dto.OperationStatisticsDTO;
import com.example.es1.entity.OperationLog;
import com.example.es1.repository.jpa.OperationLogRepository;
import com.example.es1.service.OperationLogService;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogRepository operationLogRepository;

    @Override
    @Transactional
    public void log(String operation, String docId, String keyword, HttpServletRequest request, Long durationMs, Integer status, String errorMsg) {
        try {
            OperationLog logEntity = new OperationLog();

            Integer userId = (Integer) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");

            logEntity.setUserId(userId);
            logEntity.setUsername(username);
            logEntity.setOperation(operation);
            logEntity.setDocId(docId);
            logEntity.setKeyword(keyword);
            logEntity.setRequestUrl(request.getRequestURI());
            logEntity.setRequestMethod(request.getMethod());

            if ("GET".equals(request.getMethod())) {
                String queryString = request.getQueryString();
                logEntity.setRequestParams(queryString);
            } else {
                logEntity.setRequestParams("{\"message\":\"请查看请求体\"}");
            }

            logEntity.setIpAddress(JakartaServletUtil.getClientIP(request));
            logEntity.setUserAgent(request.getHeader("User-Agent"));
            logEntity.setDurationMs(durationMs);
            logEntity.setStatus(status != null ? status : 1);
            logEntity.setErrorMsg(errorMsg);
            logEntity.setOperationTime(LocalDateTime.now());

            operationLogRepository.save(logEntity);
        } catch (Exception e) {
            log.error("记录操作日志失败：{}", e.getMessage(), e);
        }
    }

    @Override
    public void log(String operation, String docId, String keyword, HttpServletRequest request, Integer status, String errorMsg) {
        log(operation, docId, keyword, request, null, status, errorMsg);
    }

    @Override
    public void log(String operation, HttpServletRequest request) {
        log(operation, null, null, request, null, 1, null);
    }

    @Override
    public PageResult<OperationLogVO> queryLogs(OperationLogQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(queryDTO.getPageNum() - 1, queryDTO.getPageSize(), Sort.by(Sort.Direction.DESC , "operationTime"));

        Page<OperationLog> page = operationLogRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StrUtil.isNotBlank(queryDTO.getUsername())) {
                predicates.add(cb.like(root.get("username"), "%" + queryDTO.getUsername() + "%"));
            }
            if (StrUtil.isNotBlank(queryDTO.getOperation())) {
                predicates.add(cb.equal(root.get("operation"), queryDTO.getOperation()));
            }
            if (StrUtil.isNotBlank(queryDTO.getDocId())) {
                predicates.add(cb.equal(root.get("docId"), queryDTO.getDocId()));
            }
            if (queryDTO.getStartTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("operationTime"), queryDTO.getStartTime()));
            }
            if (queryDTO.getEndTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("operationTime"), queryDTO.getEndTime().with(LocalTime.MAX)));
            }
            if (queryDTO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), queryDTO.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        List<OperationLogVO> records = page.getContent().stream().map(log -> {
            OperationLogVO vo = new OperationLogVO();
            BeanUtil.copyProperties(log, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), records, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public OperationLogVO getLogDetail(Long id) {
        OperationLog log = operationLogRepository.findById(id).orElseThrow(() -> new BusinessException("日志不存在"));

        OperationLogVO vo = new OperationLogVO();
        BeanUtil.copyProperties(log, vo);
        return vo;
    }

    @Override
    public OperationStatisticsDTO getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        OperationStatisticsDTO statistics = new OperationStatisticsDTO();

        List<Object[]> operationStats = operationLogRepository.countByOperationBetween(startTime, endTime);
        List<OperationStatisticsDTO.OperationStat> operationList = operationStats.stream().map(stat -> new OperationStatisticsDTO.OperationStat(
                (String) stat[0],
                (Long) stat[1]
        )).collect(Collectors.toList());
        statistics.setOperationStats(operationList);

        List<Object[]> dailyStats = operationLogRepository.countByDayBetween(startTime, endTime);
        List<OperationStatisticsDTO.DailyStat> dailyList = dailyStats.stream().map(stat -> new OperationStatisticsDTO.DailyStat(
                stat[0].toString(),
                (Long) stat[1]
        )).collect(Collectors.toList());
        statistics.setDailyStats(dailyList);

        Pageable top10 = PageRequest.of(0, 10);
        List<Object[]> userStats = operationLogRepository.countUserActivity(startTime, endTime, top10);
        List<OperationStatisticsDTO.UserStat> userList = userStats.stream().map(stat -> new OperationStatisticsDTO.UserStat(
                (String) stat[0],
                (Long) stat[1]
        )).collect(Collectors.toList());
        statistics.setTopUsers(userList);

        List<Object[]> viewStats = operationLogRepository.countDocAccess("view", top10);
        List<OperationStatisticsDTO.DocStat> hotDocs = new ArrayList<>();
        for (Object[] stat : viewStats) {
            String docId = (String) stat[0];
            Long viewCount = (Long) stat[1];
            OperationStatisticsDTO.DocStat docStat = new OperationStatisticsDTO.DocStat();
            docStat.setDocId(docId);
            docStat.setViewCount(viewCount);
            docStat.setFileName("");
            hotDocs.add(docStat);
        }
        statistics.setHotDocs(hotDocs);

        Long totalCount = operationLogRepository.count();
        Long successCount = operationLogRepository.countByStatus(1);
        Long failCount = operationLogRepository.countByStatus(0);
        statistics.setTotalCount(totalCount);
        statistics.setSuccessCount(successCount);
        statistics.setFailCount(failCount);

        return statistics;
    }

    @Override
    public List<OperationLogVO> getRecentLogs(Integer limit, Integer userId) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "operationTime"));
        List<OperationLog> logs;

        if (userId != null) {
            logs = operationLogRepository.findByUserIdOrderByOperationTimeDesc(userId, pageable);
        } else {
            logs = operationLogRepository.findAll(pageable).getContent();
        }

        return logs.stream().map(log -> {
            OperationLogVO vo = new OperationLogVO();
            BeanUtil.copyProperties(log, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<OperationLogVO> getDocLogs(String docId) {
        return operationLogRepository.findByDocIdOrderByOperationTimeDesc(docId).stream().map(log -> {
            OperationLogVO vo = new OperationLogVO();
            BeanUtil.copyProperties(log, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cleanOldLogs(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        operationLogRepository.deleteByOperationTimeBefore(expireTime);
        log.info("清理了 {} 天前的操作日志", days);
    }
}
