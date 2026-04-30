package com.example.es1.service;

import com.example.es1.common.result.PageResult;
import com.example.es1.dto.DocumentSearchDTO;
import com.example.es1.dto.DocumentUploadDTO;
import com.example.es1.dto.DocumentVO;
import com.example.es1.dto.SearchHitVO;
import com.example.es1.entity.nfDocument;

public interface DocumentService {

    String upload(DocumentUploadDTO dto);

    PageResult<SearchHitVO> search(DocumentSearchDTO dto);

    nfDocument getDetail(String docId);

    void delete(String docId);
}
