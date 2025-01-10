package com.example.edusmile.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {



    // HTTP 보안 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/user/login", "/user/signup", "/user").permitAll()  // 특정 URL 접근 허용
                                .anyRequest().authenticated()  // 나머지 요청은 인증 필요
                )
                .csrf((csrf) -> csrf.ignoringRequestMatchers((new AntPathRequestMatcher("/h2-console/**"))))
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/user/login")
                                .defaultSuccessUrl("/mainsite", true)  // 로그인 성공 후 이동할 URL
                )
                .logout(logout ->
                        logout
                                .logoutSuccessUrl("/user/login")  // 로그아웃 후 이동할 URL
                                .invalidateHttpSession(true)  // 세션 무효화
                )
                .csrf(csrf -> csrf.disable());  // CSRF 비활성화

        return http.build();
    }

    // 패스워드 인코더 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}