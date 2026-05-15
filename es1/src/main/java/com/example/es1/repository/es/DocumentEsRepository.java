package com.example.es1.repository.es;

import com.example.es1.entity.Document;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface DocumentEsRepository extends ElasticsearchRepository<Document, String> {

    Optional<Document> findByDocId(String docId);

    List<Document> findByDocIdIn(List<String> docIds);

    void deleteByDocId(String docId);
}
