package com.duastore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
/**
 * Lớp cấu hình Spring liên quan tới web mvc config.
 */
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/")
                .setCachePeriod(86400);
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/")
                .setCachePeriod(86400);
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/")
                .setCachePeriod(86400);
        registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(86400);
    }
}
