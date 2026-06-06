package com.example.knowledgeragdemo.dto;

/**
 * 文本分类接口支持的固定分类枚举。
 *
 * <p>这里使用固定枚举，而不是把模型自由生成的分类名称直接暴露给前端，
 * 这样可以保证输出字段稳定、取值稳定，便于后续前端展示和统计分析。</p>
 */
public enum ClassificationCategory {

    BUG("缺陷问题，通常描述报错、异常、失败、无法使用等现象"),
    FEATURE("功能诉求，通常表达新增能力、优化体验或支持某种新场景"),
    QUESTION("咨询提问，通常是询问用法、原理、状态或操作方式"),
    COMPLAINT("投诉反馈，通常带有不满、抱怨或负面服务体验");

    private final String description;

    ClassificationCategory(String description) {
        this.description = description;
    }

    /**
     * 返回当前分类的固定说明文案。
     */
    public String getDescription() {
        return description;
    }
}
