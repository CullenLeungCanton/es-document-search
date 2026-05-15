package com.example.es1.repository.jpa;

import com.example.es1.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentJpaRepository extends JpaRepository<Document, Integer> {

    Optional<Document> findByDocId(String docId);

    void deleteByDocId(String docId);

    Page<Document> findAll(Specification<Document> spec, Pageable pageable);

    Page<Document> findByFileType(String fileType, Pageable pageable);
}
