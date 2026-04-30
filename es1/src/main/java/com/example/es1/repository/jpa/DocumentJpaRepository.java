package com.example.es1.repository.jpa;

import com.example.es1.entity.nfDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentJpaRepository extends JpaRepository<nfDocument, Integer> {

    Optional<nfDocument> findByDocId(String docId);

    void deleteByDocId(String docId);
}
