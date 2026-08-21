package com.example.knowledgeragdemo.dto;

import java.util.List;

/**
 * /rag/ask 接口的响应体。
 *
 * <p>这是 RAG 问答的"最终交付物"，字段设计体现了三个企业级原则：</p>
 * <ol>
 *   <li><b>答案与证据分离</b>：answer 是模型生成的文本；
 *       citations 是支撑答案的引用，两者独立返回，方便前端分别展示；</li>
 *   <li><b>可拒答</b>：enoughContext=false 时系统明确拒答，
 *       refusalReason 精确说明原因（没有资料 / 分数太低），不硬编答案；</li>
 *   <li><b>可调试</b>：retrievedChunks 把"模型到底看到了哪些片段"原样返回，
 *       出问题时先看这里，判断是召回错还是生成错。</li>
 * </ol>
 *
 * <p>为什么用普通 class + getter/setter 而不是 record？
 * 这个对象在 Service 里分步赋值（先设 retrievedChunks，再设 answer...），
 * record 是不可变的，不适合这种"构建中逐步填充"的场景。
 * 这也是 DTO 的常见选择标准。</p>
 */
public class RagAskResponse {

    /** 模型生成的答案文本。 */
    private String answer;

    /** 是否有足够上下文来回答问题；false 表示本次是拒答。 */
    private boolean enoughContext;

    /** 拒答原因：当 enoughContext=false 时说明为什么拒答。 */
    private RefusalReason refusalReason;

    /** 引用来源列表（与 retrievedChunks 相同的数据，语义上代表"答案的依据"），用于前端展示。 */
    private List<RagSearchResult> citations;

    /** 原始检索结果（模型实际看到的片段），便于调试召回质量。 */
    private List<RagSearchResult> retrievedChunks;

    /** Prompt 调试模式下返回的最终完整 prompt 文本（app.ai.prompt-debug=true 时才有值）。 */
    private String debugPrompt;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isEnoughContext() {
        return enoughContext;
    }

    public void setEnoughContext(boolean enoughContext) {
        this.enoughContext = enoughContext;
    }

    public RefusalReason getRefusalReason() {
        return refusalReason;
    }

    public void setRefusalReason(RefusalReason refusalReason) {
        this.refusalReason = refusalReason;
    }

    public List<RagSearchResult> getCitations() {
        return citations;
    }

    public void setCitations(List<RagSearchResult> citations) {
        this.citations = citations;
    }

    public List<RagSearchResult> getRetrievedChunks() {
        return retrievedChunks;
    }

    public void setRetrievedChunks(List<RagSearchResult> retrievedChunks) {
        this.retrievedChunks = retrievedChunks;
    }

    public String getDebugPrompt() {
        return debugPrompt;
    }

    public void setDebugPrompt(String debugPrompt) {
        this.debugPrompt = debugPrompt;
    }
}
