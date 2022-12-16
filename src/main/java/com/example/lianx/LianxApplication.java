package com.example.lianx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@MapperScan("com.example.lianx.mapper")
@SpringBootApplication()
public class LianxApplication {

    public static void main(String[] args) {
        SpringApplication.run(LianxApplication.class, args);
    }

}
