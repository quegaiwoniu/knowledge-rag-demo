package com.example.knowledgeragdemo.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 请求级 traceId 过滤器。
 *
 * <p>作为 Spring MVC 过滤器链的第一环，为每个 HTTP 请求做三件事：</p>
 * <ol>
 *   <li><b>生成 traceId</b> 并写入 {@link TraceIdContext}（ThreadLocal）——
 *       后续 Service 层通过 {@code TraceIdContext.get()} 随时可取；</li>
 *   <li><b>注入 SLF4J MDC</b>（Mapped Diagnostic Context）——
 *       logback 配置了 {@code %X{traceId}}，所以所有日志行会自动带上 traceId，
 *       不需要每个类手动拼接；</li>
 *   <li><b>写入响应头 {@code X-Trace-Id}</b>——前端拿到后展示，
 *       用户报问题时把 traceId 贴给后端，后端一搜日志即可定位整条链路。</li>
 * </ol>
 *
 * <p>学习要点：</p>
 * <ul>
 *   <li><b>OncePerRequestFilter</b>：保证一次请求只执行一次过滤逻辑
 *       （即使有多个过滤器映射路径也不会重复执行）；</li>
 *   <li><b>@Order(1)</b>：数值越小越先执行，保证它是最外层过滤器；</li>
 *   <li><b>finally 清理</b>：无论请求成功还是异常，都必须清 MDC 和 ThreadLocal，
 *       否则线程池复用会把 traceId 泄漏到下一个请求。</li>
 * </ul>
 */
@Component
@Order(1)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);

    /** 响应头名称（也是前端读取的 key）。 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 生成 traceId 并存入 ThreadLocal
        String traceId = TraceIdContext.generate();

        // 2. 注入 MDC：logback pattern 里的 %X{traceId} 会自动打印它
        MDC.put("traceId", traceId);

        // 3. 写响应头：前端 / 排查工具可以读取
        response.setHeader(TRACE_ID_HEADER, traceId);

        // 4. 记录请求开始（这里比拦截器更早，能覆盖所有请求）
        log.info("[{}] --> {} {}", traceId, request.getMethod(), request.getRequestURI());

        long start = System.currentTimeMillis();
        try {
            // 5. 放行到后续过滤器 / DispatcherServlet / Controller
            filterChain.doFilter(request, response);
        } finally {
            // 6. 请求结束：记录总耗时，并清理 MDC 和 ThreadLocal（关键！）
            long elapsed = System.currentTimeMillis() - start;
            log.info("[{}] <-- {} {} ({}ms)", traceId, request.getMethod(), request.getRequestURI(), elapsed);
            MDC.clear();
            TraceIdContext.clear();
        }
    }
}
