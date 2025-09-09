package com.cheolhyeon.diary.app.jwt;

import com.cheolhyeon.diary.token.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // TODO: 2. JWT 토큰 추출 메서드 구현
        // - Authorization 헤더에서 "Bearer " 접두사 제거
        // - 토큰이 존재하는지 검증
        // - 토큰 형식이 올바른지 확인

        // TODO: 3. 토큰 유효성 검증 메서드 구현
        // - JWT 토큰 서명 검증
        // - 토큰 만료 시간 확인
        // - 토큰 타입 확인 (ACCESS 토큰인지)
        // - 토큰에서 사용자 정보 추출

        // TODO: 4. 사용자 인증 정보 설정 메서드 구현
        // - 토큰에서 추출한 사용자 ID로 사용자 조회
        // - UserDetails 객체 생성 또는 User 엔티티 조회
        // - SecurityContext에 인증 정보 설정

        // TODO: 5. 예외 처리 메서드 구현
        // - 토큰이 없을 때 처리
        // - 토큰이 유효하지 않을 때 처리
        // - 사용자를 찾을 수 없을 때 처리
        // - 각 상황에 맞는 HTTP 상태 코드 및 에러 메시지 설정

        // TODO: 6. 필터 체인 통과 조건 설정
        // - 인증이 필요 없는 경로들 확인 (로그인, 회원가입, 공개 API 등)
        // - OPTIONS 요청 처리 (CORS preflight)
        // - 정적 리소스 요청 처리

        // TODO: 7. 로깅 추가
        // - 인증 성공/실패 로그
        // - 토큰 검증 과정 로그
        // - 예외 상황 로그

        // TODO: 8. 성능 최적화
        // - 불필요한 DB 조회 방지
        // - 토큰 캐싱 고려
        // - 예외 처리 최적화
        // TODO: 9. 메인 필터 로직 구현
        // 1) 요청 경로가 인증이 필요한지 확인
        // 2) Authorization 헤더에서 JWT 토큰 추출
        // 3) 토큰 유효성 검증
        // 4) 사용자 인증 정보 설정
        // 5) 다음 필터로 요청 전달
        // 6) 예외 발생 시 적절한 에러 응답 처리
    }
}
