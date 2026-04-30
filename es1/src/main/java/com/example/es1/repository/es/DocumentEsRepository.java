package com.example.es1.repository.es;

import com.example.es1.entity.nfDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface DocumentEsRepository extends ElasticsearchRepository<nfDocument, String> {

    Optional<nfDocument> findByDocId(String docId);

    List<nfDocument> findByDocIdIn(List<String> docIds);

    void deleteByDocId(String docId);
}
