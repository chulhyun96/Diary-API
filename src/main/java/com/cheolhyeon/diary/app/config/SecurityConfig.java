package com.cheolhyeon.diary.app.config;

import com.cheolhyeon.diary.auth.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 토큰 기반 인증이므로)
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 설정
                .cors(Customizer.withDefaults()) // ★ CORS 적용 (아래 Bean 사용)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                // 세션 관리 설정 (STATELESS로 설정하여 JWT 토큰 기반 인증)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // JWT 필터 추가
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // 요청별 권한 설정
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // preflight 허용
                        .requestMatchers("/*.html").permitAll()
                        .requestMatchers("/login/**").permitAll()
                        .requestMatchers("/auth/refresh").permitAll() // refresh 엔드포인트 허용
                        .requestMatchers("/js/**").permitAll() // JavaScript 파일 허용
                        .requestMatchers("/css/**").permitAll() // CSS 파일 허용
                        .requestMatchers("/favicon.ico").permitAll() // 파비콘 허용
                        .requestMatchers("/*.js").permitAll() // 루트 JS 파일 허용
                        .requestMatchers("/*.css").permitAll() // 루트 CSS 파일 허용
                        .requestMatchers("/*.map").permitAll() // 루트 소스맵 파일 허용
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/**").permitAll())
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"서비스 이용 불가. 재 로그인 하세요\"}");
                }));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 허용할 Origin 설정 (같은 컴퓨터, 다른 포트)
        configuration.setAllowedOriginPatterns(List.of(
        ));
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 허용할 헤더 (Authorization 포함)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        // 노출할 헤더 (클라이언트에서 접근 가능한 헤더)
        configuration.setExposedHeaders(List.of("Authorization"));
        // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);
        // preflight 요청 캐시 시간
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
