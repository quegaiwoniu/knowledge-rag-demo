package com.example.knowledgeragdemo.filter;

import java.util.UUID;

/**
 * 当前请求 traceId 的 ThreadLocal 工具类。
 *
 * <h3>什么是 traceId？</h3>
 * <p>一次 HTTP 请求会经过 Filter → Controller → Service → 数据库/外部模型 多个环节。
 * 没有 traceId 时，这些环节的日志散落各处，出了问题很难把"同一次请求"的日志串起来。
 * traceId 就是给每次请求发的一个"身份证号"，所有环节的日志都带上它，
 * 排查问题时按它一搜，整条调用链就出来了（这就是最小版的全链路追踪）。</p>
 *
 * <h3>为什么用 ThreadLocal？</h3>
 * <p>一个请求在同一个线程里串行执行（Tomcat 每请求一线程），
 * ThreadLocal 能让这个线程的任意位置都能拿到 traceId，而不用在方法签名里传来传去，
 * 侵入性最小。请求结束后必须 {@link #clear()}，否则线程池复用时
 * 下一个请求会读到上一个请求的 traceId（脏数据）。</p>
 */
public final class TraceIdContext {

    /** 保存当前线程的 traceId。 */
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    private TraceIdContext() {
        // 工具类私有构造器，禁止实例化
    }

    /**
     * 生成一个新的 traceId 并写入 ThreadLocal。
     *
     * <p>用 UUID 去掉横线后取前 12 位：足够随机、长度友好，
     * 日志里不会占太多空间。</p>
     *
     * @return 新生成的 traceId
     */
    public static String generate() {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        TRACE_ID.set(traceId);
        return traceId;
    }

    /**
     * 读取当前请求的 traceId。
     *
     * <p>如果当前不在 HTTP 请求上下文里（比如单元测试直接调 Service），
     * 返回 {@code "no-trace"} 而不是 null，避免调用方到处判空。</p>
     */
    public static String get() {
        String traceId = TRACE_ID.get();
        return traceId != null ? traceId : "no-trace";
    }

    /**
     * 清空 ThreadLocal。
     *
     * <p>必须在请求结束时调用（通常在 Filter 的 finally 里），
     * 防止线程池复用时 traceId 泄漏到下一个请求。</p>
     */
    public static void clear() {
        TRACE_ID.remove();
    }
}
