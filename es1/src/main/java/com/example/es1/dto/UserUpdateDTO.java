package com.example.es1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户更新参数")
public class UserUpdateDTO {

    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "部门")
    private String department;

    @Schema(description = "头像")
    private String avatar;
}
