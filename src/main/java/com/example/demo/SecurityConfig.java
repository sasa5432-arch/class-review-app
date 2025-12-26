package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // CustomUserDetailsService は自動で注入される（今はここでは特に何もしないけどOK）
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // パスワードハッシュ用
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // セキュリティ設定のメイン
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 開発中はとりあえずOFF
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/register",
                        "/login",
                        "/css/**",
                        "/js/**"
                ).permitAll()              // ここは誰でもOK
                .anyRequest().authenticated() // それ以外はログイン必須
            )
            .formLogin(login -> login
                .loginPage("/login")
                .defaultSuccessUrl("/reviews", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
