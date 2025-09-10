package com.cheolhyeon.diary.token;

import com.cheolhyeon.diary.app.jwt.JwtProvider;
import com.cheolhyeon.diary.token.dto.request.JwtRequest;
import com.cheolhyeon.diary.token.dto.response.JwtResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;


    public JwtResponse createJwt(Long kakaoId) {
        JwtRequest jwt = jwtProvider.createJwt(kakaoId);
        UsersJwt newToken = tokenRepository.save(UsersJwt.create(jwt));
        return JwtResponse.createJwtResponse(newToken.getAccessToken(), newToken.getRefreshExpiresAt());
    }
}
