package com.zhishu.config;

import com.zhishu.common.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final MediaProperties mediaProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // nas 模式指向 SMB 挂载目录，其他模式指向本机 local-dir；均以 /media/** 暴露（支持 Range）
        boolean nas = "nas".equals(mediaProperties.getMode());
        String dir = nas ? mediaProperties.getNas().getMountDir() : mediaProperties.getLocalDir();
        if (dir != null) {
            String location = "file:" + new java.io.File(dir).getAbsolutePath() + "/";
            registry.addResourceHandler("/media/**").addResourceLocations(location);
        }
    }
}