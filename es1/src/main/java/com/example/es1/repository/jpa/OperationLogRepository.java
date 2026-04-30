package com.example.es1.repository.jpa;

import com.example.es1.entity.OperationLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long>, JpaSpecificationExecutor<OperationLog> {

    List<OperationLog> findByUserIdOrderByOperationTimeDesc(Integer userId, Pageable pageable);

    List<OperationLog> findByDocIdOrderByOperationTimeDesc(String docId);

    @Query("SELECT o.operation, COUNT(o) FROM OperationLog o WHERE o.operationTime BETWEEN :startTime AND :endTime GROUP BY o.operation")
    List<Object[]> countByOperationBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT FUNCTION('DATE', o.operationTime), COUNT(o) FROM OperationLog o WHERE o.operationTime BETWEEN :startTime AND :endTime GROUP BY FUNCTION('DATE', o.operationTime) ORDER BY FUNCTION('DATE', o.operationTime)")
    List<Object[]> countByDayBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT o.username, COUNT(o) FROM OperationLog o WHERE o.operationTime BETWEEN :startTime AND :endTime GROUP BY o.username ORDER BY COUNT(o) DESC")
    List<Object[]> countUserActivity(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);

    @Query("SELECT o.docId, COUNT(o) FROM OperationLog o WHERE o.operation = :operation AND o.docId IS NOT NULL GROUP BY o.docId ORDER BY COUNT(o) DESC")
    List<Object[]> countDocAccess(@Param("operation") String operation, Pageable pageable);

    @Query("SELECT COUNT(o) FROM OperationLog o WHERE o.status = :status")
    long countByStatus(@Param("status") Integer status);

    void deleteByOperationTimeBefore(LocalDateTime time);
}
