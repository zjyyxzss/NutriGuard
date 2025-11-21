package com.nutri.guard.config; // 👈 请确认这里的包名对不对

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 添加映射路径，我们拦截一切请求
        registry.addMapping("/**")
                // 允许来自前端开发服务器的请求
                // 这里的端口一定要和你前端运行的端口一致！
                .allowedOrigins("http://localhost:5173")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 允许的请求方式
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的 Header
                .allowedHeaders("*")
                // 跨域允许时间
                .maxAge(3600);
    }
}