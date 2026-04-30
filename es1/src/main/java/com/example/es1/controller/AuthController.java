package com.example.es1.controller;

import com.example.es1.common.annotation.Log;
import com.example.es1.common.result.Result;
import com.example.es1.dto.*;
import com.example.es1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户认证", description = "用户注册、登录、个人信息管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @Log(operation = "register")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO dto) {
            userService.register(dto);
            return Result.success("注册成功", null);
    }

    @Operation(summary = "用户登录")
    @Log(operation = "login")
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
            LoginResponseDTO response = userService.login(dto);
            return Result.success(response);
    }

    @Operation(summary = "获取当前用户信息")
    @Log(operation = "get_user")
    @GetMapping("/me")
    public Result<UserInfoDTO> getCurrentUser(HttpServletRequest request) {
            Integer userId = (Integer) request.getAttribute("userId");
            UserInfoDTO userInfo = userService.getUserInfo(userId);
            return Result.success(userInfo);
    }

    @Operation(summary = "更新当前用户信息")
    @Log(operation = "update_user")
    @PutMapping("/me")
    public Result<UserInfoDTO> updateCurrentUser(HttpServletRequest request, @Valid @RequestBody UserUpdateDTO dto) {
            Integer userId = (Integer) request.getAttribute("userId");
            UserInfoDTO userInfo = userService.updateUserInfo(userId, dto);
            return Result.success(userInfo);
    }

    @Operation(summary = "修改当前用户密码")
    @Log(operation = "change_password")
    @PostMapping("/change-password")
    public Result<Void> changePassword(HttpServletRequest request, @Valid @RequestBody ChangePasswordDTO dto) {
        Integer userId = (Integer) request.getAttribute("userId");
        userService.changePassword(userId, dto);
        return Result.success("密码修改成功", null);
    }
}
