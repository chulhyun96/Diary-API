package com.cheolhyeon.diary.auth.session;

import com.cheolhyeon.diary.app.exception.session.SessionErrorStatus;
import com.cheolhyeon.diary.app.exception.session.SessionException;
import com.cheolhyeon.diary.auth.entity.AuthSession;
import com.cheolhyeon.diary.auth.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    JwtProvider jwtProvider;

    @Mock
    SessionRepository sessionRepository;

    @Mock
    HttpServletResponse response;

    @InjectMocks
    SessionService sessionService;

    private final String testSid = "test-session-id";
    private final String testRtPlain = "test-refresh-token";
    private final String testCurrentHash = "current-hash";
    private final Long testUserId = 1L;
    private final LocalDateTime testNow = LocalDateTime.now();


    @Test
    @DisplayName("CASE2: hash(rtPlain) == session.currentHash - 성공적인 토큰 갱신")
    void refreshSession_CurrentHashMatch_Success() throws NoSuchAlgorithmException, InvalidKeyException {
        // Given
        AuthSession mockSession = mock(AuthSession.class);
        given(mockSession.getRtHashCurrent()).willReturn(testCurrentHash);
        given(mockSession.getUserId()).willReturn(testUserId);
        
        given(sessionRepository.findActiveSessionById(testSid, testNow)).willReturn(Optional.of(mockSession));
        given(jwtProvider.hashRT(testRtPlain)).willReturn(testCurrentHash);
        given(jwtProvider.generateOpaqueRT()).willReturn("new-refresh-token");
        given(jwtProvider.hashRT("new-refresh-token")).willReturn("new-hash");
        given(jwtProvider.generateAccessToken(testUserId, testSid)).willReturn("new-access-token");

        // When
        sessionService.refreshSession(testSid, testRtPlain, testNow, response);

        // Then
        verify(jwtProvider).generateOpaqueRT();
        verify(jwtProvider).hashRT("new-refresh-token");
        verify(jwtProvider).generateAccessToken(testUserId, testSid);
        verify(response).setHeader("Authorization", "Bearer new-access-token");
        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        verify(mockSession).refresh("new-hash", testCurrentHash);
        verify(sessionRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("CASE1: hash(rtPlain) != session.currentHash - 세션 삭제")
    void refreshSession_CurrentHashMismatch_DeleteSession() throws NoSuchAlgorithmException, InvalidKeyException {
        // Given
        AuthSession mockSession = mock(AuthSession.class);
        given(mockSession.getRtHashCurrent()).willReturn(testCurrentHash);
        
        given(sessionRepository.findActiveSessionById(testSid, testNow)).willReturn(Optional.of(mockSession));
        given(jwtProvider.hashRT(testRtPlain)).willReturn("different-hash");

        // When & Then
        assertThatThrownBy(() -> sessionService.refreshSession(testSid, testRtPlain, testNow, response))
                .isInstanceOf(SessionException.class)
                .hasFieldOrPropertyWithValue("errorStatus", SessionErrorStatus.SESSION_EXPIRED);

        verify(sessionRepository).deleteById(testSid);
        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    @DisplayName("CASE3: sessionRepository.findActiveSessionById가 empty - 401 예외")
    void refreshSession_SessionNotFound_ThrowsException() {
        // Given
        given(sessionRepository.findActiveSessionById(testSid, testNow)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sessionService.refreshSession(testSid, testRtPlain, testNow, response))
                .isInstanceOf(SessionException.class)
                .hasFieldOrPropertyWithValue("errorStatus", SessionErrorStatus.SESSION_EXPIRED);
        
        verify(sessionRepository, never()).deleteById(anyString());
        verify(jwtProvider, never()).generateOpaqueRT();
        verify(jwtProvider, never()).generateAccessToken(any(), any());
    }

    @Test
    @DisplayName("CASE4: hash(rtPlain)이 currentHash와 불일치 - 세션 삭제")
    void refreshSession_HashMismatch_DeleteSession() throws NoSuchAlgorithmException, InvalidKeyException {
        // Given
        AuthSession mockSession = mock(AuthSession.class);
        given(mockSession.getRtHashCurrent()).willReturn(testCurrentHash);
        
        given(sessionRepository.findActiveSessionById(testSid, testNow)).willReturn(Optional.of(mockSession));
        given(jwtProvider.hashRT(testRtPlain)).willReturn("different-hash");

        // When & Then
        assertThatThrownBy(() -> sessionService.refreshSession(testSid, testRtPlain, testNow, response))
                .isInstanceOf(SessionException.class)
                .hasFieldOrPropertyWithValue("errorStatus", SessionErrorStatus.SESSION_EXPIRED);

        verify(sessionRepository).deleteById(testSid);
        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    @DisplayName("setAccessTokenToHeader 메서드 테스트")
    void setAccessTokenToHeader_Success() {
        // Given
        given(jwtProvider.generateAccessToken(testUserId, testSid)).willReturn("test-access-token");

        // When
        String result = sessionService.setAccessTokenToHeader(testUserId, testSid, response);

        // Then
        assertThat(result).isEqualTo("test-access-token");
        verify(response).setHeader("Authorization", "Bearer test-access-token");
    }

}