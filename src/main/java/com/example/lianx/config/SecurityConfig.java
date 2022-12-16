package com.example.lianx.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 仅仅作为演示
        return (web) -> web.ignoring().requestMatchers("/resources/**");
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authorize-> {
                    try {
                        authorize
                                // 放行登录接口
                                .requestMatchers("/login").permitAll()
                                // 放行资源目录
                                .requestMatchers("/resources/**").permitAll()
                                // 其余的都需要权限校验
                                .anyRequest().authenticated()
                                // 防跨站请求伪造
                                .and().csrf(csrf -> csrf.disable());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        ).build();
    }
}