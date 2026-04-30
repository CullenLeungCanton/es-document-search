package com.example.es1.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "documents")
@Document(indexName = "nf_documents")
public class nfDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "doc_id", unique = true, nullable = false, length = 50)
    @Field(type = FieldType.Keyword)
    private String docId;

    @Column(name = "file_name", nullable = false, length = 500)
    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String fileName;

    @Column(name = "file_type", length = 20)
    @Field(type = FieldType.Keyword)
    private String fileType;

    @Column(name = "file_size")
    @Field(type = FieldType.Long)
    private Long fileSize;

    @Column(name = "file_path", length = 500)
    @Field(type = FieldType.Keyword, index = false)
    private String filePath;

    @Column(name = "category", length = 50)
    @Field(type = FieldType.Keyword)
    private String category;

    @Column(name = "region", length = 100)
    @Field(type = FieldType.Keyword)
    private String region;

    @Column(name = "tags", length = 500)
    @Field(type = FieldType.Keyword)
    private String tags;

    @Column(name = "view_count")
    @Field(type = FieldType.Integer)
    private Integer viewCount = 0;

    @Column(name = "download_count")
    @Field(type = FieldType.Integer)
    private Integer downloadCount = 0;

    @Column(name = "upload_time")
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    @Column(name = "upload_user", length = 50)
    @Field(type = FieldType.Keyword)
    private String uploadUser;

    @Transient
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String content;

    @Transient
    private String highlight;
}
