package com.example.knowledgeragdemo.exception;

import com.example.knowledgeragdemo.dto.ApiResponse;
import com.example.knowledgeragdemo.filter.TraceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局统一异常处理。
 *
 * <p>企业项目里不应该让每个 Controller 自己 try-catch（代码重复、容易漏）。
 * 用 {@code @RestControllerAdvice} 做一个全局兜底，任何 Controller 抛出的异常
 * 都会自动走到这里，统一转换成 {@link ApiResponse} 格式返回。</p>
 *
 * <p>学习要点：</p>
 * <ul>
 *   <li><b>@RestControllerAdvice</b>：AOP 思想（面向切面）的典型应用——
 *       把"异常转响应"这个横切关注点集中到一处；</li>
 *   <li><b>@ExceptionHandler(Xxx.class)</b>：声明这个方法处理哪种异常。
 *       多个方法按异常类型精确匹配，子类异常优先于父类；</li>
 *   <li><b>携带 traceId</b>：错误响应带上当前请求的 traceId，
 *       用户报错时直接贴 ID，后端就能定位到对应日志，这是可观测性的关键闭环。</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 兜底异常处理（捕获所有未处理的 Exception）。
     *
     * <p>返回 500 + 统一错误体。这里会记录完整堆栈（第三个参数 e），
     * 便于后端排查；但返回给前端的是精简 message，不暴露堆栈细节。</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        String traceId = TraceIdContext.get();
        // error 级别 + 堆栈：生产环境排查问题全靠这条日志
        log.error("[{}] Unhandled exception: {}", traceId, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Trace-Id", traceId)
                .body(ApiResponse.failure("服务内部错误 [traceId=" + traceId + "]: " + e.getMessage()));
    }

    /**
     * 参数/状态非法（IllegalArgumentException）→ 400。
     *
     * <p>和上面的 Exception 处理分开，是因为"请求有问题"（4xx，客户端责任）
     * 和"服务出问题"（5xx，服务端责任）语义不同，HTTP 状态码必须区分。</p>
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        String traceId = TraceIdContext.get();
        // warn 级别：这类错误是预期的客户端问题，不需要惊动告警
        log.warn("[{}] Bad request: {}", traceId, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("X-Trace-Id", traceId)
                .body(ApiResponse.failure(e.getMessage()));
    }
}
