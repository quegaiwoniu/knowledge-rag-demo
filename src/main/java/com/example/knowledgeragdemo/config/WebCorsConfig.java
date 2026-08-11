package com.example.knowledgeragdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层跨域配置。
 *
 * <p>当前前端本地运行在 Vite 开发服务器上，后端运行在 Spring Boot 本地端口上，
 * 两者属于不同源。浏览器会默认拦截这种跨域请求，所以这里显式放开本地开发环境
 * 需要的访问来源。</p>
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    /**
     * 允许前端开发服务器访问当前后端接口。
     *
     * <p>只允许本机开发地址，但不绑定 Vite 的具体端口，避免端口占用时
     * 前端切换端口后被浏览器拦截。</p>
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://127.0.0.1:*", "http://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
