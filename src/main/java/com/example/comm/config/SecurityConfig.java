package com.example.comm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import reactor.core.publisher.Mono;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/ws/chat").permitAll() // 允许所有人访问 WebSocket 路径
                        .anyExchange().authenticated() // 其他路径需要身份验证
                )
                .httpBasic(withDefaults())
                .csrf(csrf -> csrf.disable()); // 禁用 CSRF 保护，WebSocket 通常不需要; // 使用 HTTP Basic 认证

        return http.build();
    }

//    public ReactiveAuthenticationManager customReactiveAuthenticationManager() {
//        return new ReactiveAuthenticationManager() {
//            @Override
//            public Mono<Authentication> authenticate(Authentication authentication) {
//                return null;
//            }
//        }
//    }
}
