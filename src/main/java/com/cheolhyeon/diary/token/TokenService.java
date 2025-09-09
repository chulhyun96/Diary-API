package com.cheolhyeon.diary.token;

import com.cheolhyeon.diary.app.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;
}
