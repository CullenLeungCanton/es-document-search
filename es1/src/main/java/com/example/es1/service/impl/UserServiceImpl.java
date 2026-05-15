package com.example.es1.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.example.es1.common.exception.BusinessException;
import com.example.es1.common.result.PageResult;
import com.example.es1.common.utils.JwtUtil;
import com.example.es1.common.utils.UserContextUtil;
import com.example.es1.dto.*;
import com.example.es1.entity.User;
import com.example.es1.repository.jpa.UserRepository;
import com.example.es1.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public User register(UserRegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setDepartment(dto.getDepartment());
        user.setRole("user");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());

        User saveUser = userRepository.save(user);
        log.info("用户注册成功：username={}", saveUser.getUsername());

        return saveUser;
    }

    @Override
    public LoginResponseDTO login(LoginDTO dto) {

        User user = userRepository.findByUsername(dto.getUsername()).orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        log.info("用户登录成功：username={}", user.getUsername());

        return new LoginResponseDTO(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole(),
                user.getAvatar(),
                user.getDepartment()
        );
    }

    @Override
    public UserInfoDTO getUserInfo(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));

        UserInfoDTO dto = new UserInfoDTO();
        BeanUtil.copyProperties(user, dto);
        return dto;
    }

    @Override
    @Transactional
    public UserInfoDTO updateUserInfo(Integer userId, UserUpdateDTO dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));

        if (StrUtil.isNotBlank(dto.getRealName())) {
            user.setRealName(dto.getRealName());
        }
        if (StrUtil.isNotBlank(dto.getDepartment())) {
            user.setDepartment(dto.getDepartment());
        }
        if (StrUtil.isNotBlank(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
        }

        user.setUpdateTime(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        UserInfoDTO result = new UserInfoDTO();
        BeanUtil.copyProperties(savedUser, result);
        return result;
    }

    @Override
    @Transactional
    public void changePassword(Integer userId, ChangePasswordDTO dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        log.info("用户密码修改成功：userId={}", userId);
    }

    @Override
    @Transactional
    public void resetPassword(Integer userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        log.info("用户密码重置成功：userId={}", userId);
    }

    @Override
    @Transactional
    public void updateUserStatus(Integer userId, Integer status) {
        Integer currentUserId = UserContextUtil.getCurrentUserId();
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值只能是0或1");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));

        if (status == 0 && userId.equals(currentUserId)) {
            throw new BusinessException("不能禁用当前登录账号");
        }

        if (status == 0 && user.getRole().equals("admin") && user.getId() == 1) {
            throw new BusinessException("超级管理员不能被禁用");
        }

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        log.info("用户状态更新：userId={}, status={}", userId, status);
    }

    @Override
    public PageResult<User> getUserList(Integer pageNum, Integer pageSize, String keyword, String role, Integer status) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<User> page;

        if (StrUtil.isNotBlank(keyword)) {
            page = userRepository.findByUsernameContainingOrRealNameContaining(keyword, keyword, pageable);
        } else if (StrUtil.isNotBlank(role) && status != null) {
            page = userRepository.findByRoleAndStatus(role, status, pageable);
        } else if (StrUtil.isNotBlank(role)) {
            page = userRepository.findByRole(role, pageable);
        } else if (status != null) {
            page = userRepository.findByStatus(status, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }

        return PageResult.of(page.getTotalElements(), page.getContent(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));

        userRepository.delete(user);
        log.info("用户删除成功：userId={}, username={}", userId, user.getUsername());
    }
}
