package com.example.es1;

import com.example.es1.entity.User;
import com.example.es1.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRealName("系统管理员");
            admin.setRole("admin");
            admin.setStatus(1);
            admin.setCreateTime(LocalDateTime.now());
            userRepository.save(admin);

            System.out.println("==========");
            System.out.println("管理员账号已创建");
            System.out.println("用户名：" + adminUsername);
            System.out.println("密码：" + adminPassword);
            System.out.println("==========");
        }
    }
}
