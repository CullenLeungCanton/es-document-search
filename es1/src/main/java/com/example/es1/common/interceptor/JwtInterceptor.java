package com.example.es1.common.interceptor;

import com.example.es1.common.exception.BusinessException;
import com.example.es1.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        if (isPublicPath(request.getRequestURI())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或token已过期");
        }

        token = token.substring(7);

        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "token无效或已过期");
        }

        String username = jwtUtil.getUsernameFromToken(token);
        Integer userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        if (isAdminPath(request.getRequestURI())) {
            if (!"admin".equals(role)) {
                throw new BusinessException(403, "权限不足，需要管理员权限");
            }
        }

        return true;
    }

    private boolean isPublicPath(String uri) {
        return uri.startsWith("api/auth/login") || uri.startsWith("api/auth/register") || uri.startsWith("/doc.html") || uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs") || uri.startsWith("/webjars");
    }

    private boolean isAdminPath(String uri) {
        return uri.startsWith("/api/admin") || uri.startsWith("/api/documents/admin") || uri.startsWith("/api/audit");
    }

}