package com.turbine.tnd;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan(value = "com.turbine.tnd.dao")
@EnableTransactionManagement
@SpringBootApplication(scanBasePackages="com.turbine.tnd.*")
public class TndApplication {

    public static void main(String[] args) {
        SpringApplication.run(TndApplication.class, args);
    }

}
