package com.example.es1.controller;

import com.example.es1.common.annotation.Log;
import com.example.es1.common.result.PageResult;
import com.example.es1.common.result.Result;
import com.example.es1.entity.User;
import com.example.es1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员接口", description = "用户管理（仅管理员）")
@RestController
@RequestMapping("api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @Operation(summary = "获取用户列表")
    @Log(operation = "list_users")
    @GetMapping("/list")
    public Result<PageResult<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        PageResult<User> result = userService.getUserList(pageNum, pageSize, keyword, role, status);
        return Result.success(result);
    }

    @Operation(summary = "重置用户密码")
    @Log(operation = "reset_password")
    @PostMapping("/{userId}/reset-password")
    public Result<Void> resetPassword(@PathVariable Integer userId, @RequestParam(defaultValue = "123456") String newPassword) {
        userService.resetPassword(userId, newPassword);
        return Result.success("密码重置成功，新密码：" + newPassword, null);
    }

    @Operation(summary = "禁用/启用用户")
    @Log(operation = "update_user_status")
    @PutMapping("/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Integer userId, @RequestParam Integer status) {
        userService.updateUserStatus(userId, status);
        String message = status == 1 ? "用户已启用" : "用户已禁用";
        return Result.success(message ,null);
    }

    @Operation(summary = "删除用户")
    @Log(operation = "delete_user")
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return Result.success("用户删除成功", null);
    }
}
