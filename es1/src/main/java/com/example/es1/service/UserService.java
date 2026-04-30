package com.example.es1.service;

import com.example.es1.common.result.PageResult;
import com.example.es1.dto.*;
import com.example.es1.entity.User;

public interface UserService {

    User register(UserRegisterDTO dto);

    LoginResponseDTO login(LoginDTO dto);

    UserInfoDTO getUserInfo(Integer userId);

    UserInfoDTO updateUserInfo(Integer userId, UserUpdateDTO dto);

    void changePassword(Integer userId, ChangePasswordDTO dto);

    void resetPassword(Integer userId, String newPassword);

    void updateUserStatus(Integer userId, Integer status);

    PageResult<User> getUserList(Integer pageNum, Integer pageSize, String keyword, String role, Integer status);

    void deleteUser(Integer userId);
}
