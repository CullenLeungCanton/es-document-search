package com.example.es1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "文档上传参数")
public class DocumentUploadDTO {

    @NotNull(message = "文件不能为空")
    @Schema(description = "上传的文件", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "区域")
    private String region;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "上传者")
    private String uploadUser;
}
