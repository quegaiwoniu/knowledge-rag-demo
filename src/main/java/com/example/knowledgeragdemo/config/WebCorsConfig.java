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
     * <p>这里只放开本地开发常见地址，避免把规则写得过宽。</p>
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://127.0.0.1:5173", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
