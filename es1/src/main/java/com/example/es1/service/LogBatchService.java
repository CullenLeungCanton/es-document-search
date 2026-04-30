package com.example.es1.service;

import com.example.es1.entity.OperationLog;
import com.example.es1.repository.jpa.OperationLogRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogBatchService {

    private final OperationLogRepository operationLogRepository;

    private final BlockingQueue<OperationLog> logQueue = new LinkedBlockingQueue<>(5000);

    private static final int BATCH_SIZE = 100;

    private volatile boolean running = true;

    public void offer(OperationLog logs) {
        boolean success = logQueue.offer(logs);
        if (!success) {
            log.warn("日志队列已满，丢弃日志：{}", logs.getOperation());
        }
    }

    @PostConstruct
    public void startBatchConsumer () {
        Thread batchThread = new Thread(() -> {
            List<OperationLog> batch = new ArrayList<>(BATCH_SIZE);

            while (running) {
                try {
                    OperationLog logs = logQueue.poll(1, TimeUnit.SECONDS);
                    if (logs != null) {
                        batch.add(logs);
                        logQueue.drainTo(batch, BATCH_SIZE - batch.size());

                        if (!batch.isEmpty()) {
                            saveBatch(batch);
                            batch.clear();
                        }
                    } else {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("批量插入日志失败", e);
                }
            }

            if (!logQueue.isEmpty()) {
                List<OperationLog> remaining = new ArrayList<>();
                logQueue.drainTo(remaining);
                if (!remaining.isEmpty()) {
                    saveBatch(remaining);
                }
            }
        });
        batchThread.setName("log-batch-writer");
        batchThread.setDaemon(true);
        batchThread.start();
        log.info("日志批量写入线程启动，批量大小：{}",BATCH_SIZE);
    }

    private void saveBatch(List<OperationLog> batch) {
        try {
            operationLogRepository.saveAll(batch);
            log.debug("批量写入 {} 条日志", batch.size());
        } catch (Exception e) {
            log.error("批量写入日志失败，尝试单条写入", e);
            for (OperationLog logs : batch) {
                try {
                    operationLogRepository.save(logs);
                } catch (Exception ex) {
                    log.error("单条日志写入失败：{}", logs.getOperation(), ex);
                }
            }
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void flushRemainingLogs() {
        if (!logQueue.isEmpty()) {
            List<OperationLog> remaining = new ArrayList<>();
            logQueue.drainTo(remaining);
            if (!remaining.isEmpty()) {
                saveBatch(remaining);
                log.info("定时刷新写入 {} 条日志", remaining.size());
            }
        }
    }

    public int getQueueSize() {
        return logQueue.size();
    }

    @PreDestroy
    public void destroy() {
        running = false;
        log.info("日志批量写入服务关闭");
    }
}
