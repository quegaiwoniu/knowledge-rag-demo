package com.example.knowledgeragdemo.dto;

/**
 * RAG 问答拒答原因枚举。
 *
 * <p>当 {@code enoughContext=false} 时，系统拒绝回答而不是硬编答案。
 * 但"拒答"也分不同原因，用枚举精确表达，方便：</p>
 * <ul>
 *   <li>前端展示具体原因（如"知识库没有相关内容" vs "内容相关度太低"）；</li>
 *   <li>后端排障（定位是语料缺失问题，还是 embedding/检索质量问题）；</li>
 *   <li>评测自动化（断言拒答行为是否符合预期）。</li>
 * </ul>
 *
 * <p>枚举 vs 字符串：用枚举可以把合法取值限定在固定集合里，
 * 编译期就能发现错误，是结构化输出的企业实践（与 Day 3 分类、Day 4 抽取同理）。</p>
 */
public enum RefusalReason {

    /** 检索结果为空：知识库中没有任何与问题相关的片段。 */
    NO_RETRIEVED_CHUNKS("检索结果为空"),

    /** 召回的片段相似度都低于阈值：有内容但都不够相关，强行回答就是编造。 */
    LOW_SIMILARITY_SCORE("召回片段相似度低于阈值"),

    /**
     * 召回片段与问题不相关（预留）。
     *
     * <p>当前版本通过相似度阈值已能拦截大多数无关内容；
     * 这个值预留给未来引入"相关性重排/模型判断"后使用。</p>
     */
    CONTENT_MISMATCH("召回片段与问题不相关");

    /** 人类可读的中文描述，直接用于前端展示或日志。 */
    private final String description;

    RefusalReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
