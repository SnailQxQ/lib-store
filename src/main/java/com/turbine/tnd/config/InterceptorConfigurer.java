package com.turbine.tnd.config;

import com.turbine.tnd.intercepter.UserInterceptor;
import com.turbine.tnd.intercepter.fileInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/9 14:34
 */
@Configuration
public class InterceptorConfigurer implements WebMvcConfigurer {

    @Bean
    public HandlerInterceptor createFileInterceptor(){
        return new fileInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        HandlerInterceptor userInterceptor = new UserInterceptor();
        //让这个bean提前初始化
        HandlerInterceptor fileInterceptor = createFileInterceptor();
        List<String> patterns = new ArrayList<>();
        patterns.add("/index.html");
        patterns.add("/error/**");
        patterns.add("/user/login/**");
        //patterns.add("/user/downLoad/*");

        registry.addInterceptor(userInterceptor).addPathPatterns("/**").excludePathPatterns(patterns);
        registry.addInterceptor(fileInterceptor).addPathPatterns("/user/resource/**").excludePathPatterns(patterns);

    }
}
