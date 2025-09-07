/*
package com.cheolhyeon.diary.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (API 서버이므로)
            .csrf(AbstractHttpConfigurer::disable)
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // OAuth2 Client 비활성화 (수동으로 카카오 API 호출하므로)
            .oauth2Client(AbstractHttpConfigurer::disable)
            // 세션 관리 설정 (STATELESS로 설정하여 JWT 토큰 기반 인증)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 요청별 권한 설정
            .authorizeHttpRequests(authz -> authz
                // 공개 엔드포인트 (카카오 로그인 관련)
                .requestMatchers("/login/oauth2/code/kakao").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                // 카카오 OAuth2 리다이렉트 URL (설정값에 따라 동적 처리)
                .requestMatchers("/oauth2/code/kakao").permitAll()
                // Spring Boot 기본 에러 페이지
                .requestMatchers("/error").permitAll()
                // 브라우저 자동 요청 경로들 (Chrome DevTools, favicon 등)
                .requestMatchers("/.well-known/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/robots.txt").permitAll()
                // 헬스체크 엔드포인트
                .requestMatchers("/actuator/health").permitAll()
                // Swagger UI (개발 환경에서만)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 기타 모든 API 요청은 인증 필요
                .requestMatchers("/api/**").authenticated()
                // 나머지 요청은 허용
                .anyRequest().permitAll()
            )
            // 예외 처리 설정
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다.\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"접근 권한이 없습니다.\"}");
                })
            );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 서버 허용
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000"));
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);
        // preflight 요청 캐시 시간
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
*/
