package com.turbine.tnd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/25 21:58
 */
@Configuration
//@ConfigurationProperties(prefix = "file.setting")
public class UploadFileConfig {
    //根目录 /static
    @Value("${file.upload.baseDir}")
    String baseDir;


    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //设置为绝对路径，要设置盘符
        System.out.println("baseDir"+baseDir);
        factory.setLocation(baseDir);
        //文件最大
       // factory.setMaxFileSize("5MB");
        // 设置总上传数据总大小
       // factory.setMaxRequestSize("10MB");
        return factory.createMultipartConfig();
    }
}
