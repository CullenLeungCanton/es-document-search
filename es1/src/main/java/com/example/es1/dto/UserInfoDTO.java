package com.example.es1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息响应")
public class UserInfoDTO {
    private Integer id;
    private String username;
    private String realName;
    private String role;
    private String department;
    private String avatar;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime lastLogin;
}
