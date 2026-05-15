package com.example.es1.service;

import com.example.es1.common.result.PageResult;
import com.example.es1.dto.DocumentSearchDTO;
import com.example.es1.dto.DocumentUploadDTO;
import com.example.es1.dto.SearchHitVO;
import com.example.es1.entity.Document;
import jakarta.servlet.http.HttpServletRequest;

public interface DocumentService {

    String upload(DocumentUploadDTO dto);

    PageResult<SearchHitVO> search(DocumentSearchDTO dto);

    Document getDetail(String docId, HttpServletRequest request);

    void delete(String docId);
}
