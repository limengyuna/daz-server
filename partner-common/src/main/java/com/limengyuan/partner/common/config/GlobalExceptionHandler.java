package com.limengyuan.partner.common.config;

import com.limengyuan.partner.common.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一拦截各类异常，返回结构化的 Result 响应，避免裸抛堆栈信息给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验失败 (@Valid 注解触发)
     * 例如：@NotBlank、@Size、@Min 等校验不通过
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[参数校验失败] {}", message);
        return Result.error(400, message);
    }

    /**
     * 路径参数/Query参数 校验失败 (@Validated + @Min/@Max 等)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("[约束校验失败] {}", message);
        return Result.error(400, message);
    }

    /**
     * 请求体 JSON 解析失败（格式错误、字段类型不匹配等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleJsonParseException(HttpMessageNotReadableException ex) {
        log.warn("[JSON解析失败] {}", ex.getMessage());
        return Result.error(400, "请求体格式错误，请检查 JSON 格式");
    }

    /**
     * 缺少必要的请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("[缺少参数] {}", ex.getParameterName());
        return Result.error(400, "缺少必要参数: " + ex.getParameterName());
    }

    /**
     * 参数类型转换失败（如路径参数传了非数字）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("[参数类型错误] 参数 {} 期望类型 {}", ex.getName(), ex.getRequiredType());
        return Result.error(400, "参数类型错误: " + ex.getName());
    }

    /**
     * 请求方法不支持（如 GET 接口用了 POST 请求）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return Result.error(405, "不支持的请求方法: " + ex.getMethod());
    }

    /**
     * 数据库唯一键冲突等数据完整性异常（如重复报名、重复关注等）
     * 使用 RuntimeException 匹配，避免对 spring-dao 的编译期依赖
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException ex) {
        // 检查是否为数据库唯一键冲突类异常
        String className = ex.getClass().getName();
        if (className.contains("DuplicateKey") || className.contains("DataIntegrityViolation")) {
            log.warn("[数据完整性冲突] {}", ex.getMessage());
            return Result.error(409, "数据已存在，请勿重复操作");
        }
        // 其他运行时异常交给兜底处理
        log.error("[运行时异常] ", ex);
        return Result.error(500, "服务器内部错误，请稍后重试");
    }

    /**
     * 资源未找到（404）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNotFound(NoResourceFoundException ex) {
        return Result.error(404, "请求的资源不存在");
    }

    /**
     * 兜底处理：未被上面捕获的所有异常
     * 记录完整堆栈日志，但只返回通用错误信息给前端，防止信息泄露
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("[系统异常] ", ex);
        return Result.error(500, "服务器内部错误，请稍后重试");
    }
}
