package com.cookmate.global.security;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 실제 규칙을 적용하는 정승 세워놓기
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    // passwordEncoder - BCrypt 해싱 알고리즘 사용
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // API 규칙 적용
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                // disable Browser Security CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // disable Basic HTTP Login
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 세션 정보를 저장하지 않음(jwt에서는 임시 세션 정보 사용, 사용된 세션은 이후 초기화 -> STATELESS 사용)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // API 주소별 접근 권한 설정
                // hasAuthority도 가능
                .authorizeHttpRequests(auth -> auth
                        // 사용자 전용 API
                        .requestMatchers("/api/user/signup", "/api/user/login").permitAll().requestMatchers("/api/user/**").hasAnyAuthority("USER","ADMIN")
                        // 관리자 전용 API.
                        .requestMatchers("/api/admin/signup", "/api/admin/login").permitAll().requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // 그 외의 모든 요청은 인증 요구
                        .anyRequest().authenticated()
                )

             
                // JwtAuthenticationFilter before 적용
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
