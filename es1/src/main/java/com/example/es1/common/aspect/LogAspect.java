package com.example.es1.common.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.alibaba.fastjson2.JSON;
import com.example.es1.common.annotation.Log;
import com.example.es1.entity.OperationLog;
import com.example.es1.service.LogBatchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final LogBatchService logBatchService;

    @Pointcut("@annotation(com.example.es1.common.annotation.Log)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);

        String docId = extractDocId(point, logAnnotation);
        String keyword = extractKeyword(point, logAnnotation);

        OperationLog operationLog = new OperationLog();
        operationLog.setOperationTime(LocalDateTime.now());

        String operation = logAnnotation.operation();
        if (operation.isEmpty()) {
            operation = method.getName();
        }
        operationLog.setOperation(operation);

        if (request != null) {
            Integer userId = (Integer) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");
            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            operationLog.setDocId(docId);
            operationLog.setKeyword(keyword);
            operationLog.setIpAddress(JakartaServletUtil.getClientIP(request));
            operationLog.setUserAgent(request.getHeader("User-Agent"));
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setRequestMethod(request.getMethod());
        }

        if (logAnnotation.recordParams() && request != null) {
            String params = getRequestParams(request, point);
            if (params != null && params.length() > 500) {
                params = params.substring(0, 500);
            }
            operationLog.setRequestParams(params);
        }

        Object result = null;
        try {
            result = point.proceed();
            operationLog.setStatus(1);

            if (logAnnotation.recordResult() && result != null) {
                String resultStr = JSON.toJSONString(result);
                if (resultStr.length() > 500) {
                    resultStr = resultStr.substring(0, 500);
                }
                operationLog.setRequestParams(operationLog.getRequestParams() + " | 返回：" + resultStr);
            }
            return result;
        } catch (Exception e) {
            operationLog.setStatus(0);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500);
            }
            operationLog.setErrorMsg(errorMsg);
            throw e;
        } finally {
            if (logAnnotation.recordDuration()) {
                operationLog.setDurationMs(System.currentTimeMillis() - startTime);
            }

            logBatchService.offer(operationLog);
        }
    }

    private String getRequestParams(HttpServletRequest request, ProceedingJoinPoint point) {
        Map<String, Object> params = new HashMap<>();

        if ("GET".equals(request.getMethod())) {
            String queryString = request.getQueryString();
            if (queryString != null && !queryString.isEmpty()) {
                return queryString;
            }
        }

        Object[] args = point.getArgs();
        String[] paramNames = getParameterNames(point);

        if (paramNames != null && args != null && paramNames.length == args.length) {
            for (int i = 0; i < paramNames.length; i++) {
                Object arg = args[i];
                if (arg != null && !(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse) && !(arg instanceof MultipartFile)) {
                    String value = JSON.toJSONString(arg);
                    if (value.length() > 200) {
                        value = value.substring(0, 200) + "...";
                    }
                    params.put(paramNames[i], value);
                }
            }
        }

        if (params.isEmpty()) {
            return null;
        }

        return JSON.toJSONString(params);
    }

    private String[] getParameterNames(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        Parameter[] parameters = method.getParameters();
        String[] paramNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            paramNames[i] = parameters[i].getName();
        }

        if (paramNames.length > 0 && !paramNames[0].startsWith("arg")) {
            return paramNames;
        }

        String[] defaultNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            defaultNames[i] = "arg" + i;
        }
        return defaultNames;
    }

    private String extractDocId(ProceedingJoinPoint point, Log logAnnotation) {
        String docIdParam = logAnnotation.docIdParam();
        if (StrUtil.isBlank(docIdParam)) {
            return null;
        }

        Object[] args = point.getArgs();
        Parameter[] parameters = ((MethodSignature) point.getSignature()).getMethod().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            if (docIdParam.equals(param.getName())) {
                return args[i] != null ? args[i].toString() : null;
            }

            if (param.isAnnotationPresent(PathVariable.class)) {
                PathVariable pv = param.getAnnotation(PathVariable.class);
                if (docIdParam.equals(pv.value()) || docIdParam.equals(param.getName())) {
                    return args[i] != null ? args[i].toString() : null;
                }
            }

            if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = param.getAnnotation(RequestParam.class);
                if (docIdParam.equals(rp.value()) || docIdParam.equals(param.getName())) {
                    return args[i] != null ? args[i].toString() : null;
                }
            }

            if (args[i] != null && !isBasicType(args[i])) {
                try {
                    Field field = args[i].getClass().getDeclaredField(docIdParam);
                    field.setAccessible(true);
                    Object value = field.get(args[i]);
                    if (value != null) {
                        return value.toString();
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {

                }
            }
        }

        return null;
    }

    private String extractKeyword(ProceedingJoinPoint point, Log logAnnotation) {
        String keywordParam = logAnnotation.keywordParam();
        if (StrUtil.isBlank(keywordParam)) {
            return null;
        }

        Object[] args = point.getArgs();
        Parameter[] parameters = ((MethodSignature) point.getSignature()).getMethod().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            if (keywordParam.equals(param.getName())) {
                return args[i] != null ? args[i].toString() : null;
            }


            if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = param.getAnnotation(RequestParam.class);
                if (keywordParam.equals(rp.value()) || keywordParam.equals(param.getName())) {
                    return args[i] != null ? args[i].toString() : null;
                }
            }

            if (args[i] != null && !isBasicType(args[i])) {
                try {
                    Field field = args[i].getClass().getDeclaredField(keywordParam);
                    field.setAccessible(true);
                    Object value = field.get(args[i]);
                    if (value != null) {
                        return value.toString();
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {

                }
            }
        }

        return null;
    }

    private boolean isBasicType(Object obj) {
        return obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Character || obj instanceof Date || obj instanceof LocalDateTime || obj.getClass().isPrimitive();
    }
}
