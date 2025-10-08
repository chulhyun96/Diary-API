package com.cheolhyeon.diary.friendrequest.service;

import com.cheolhyeon.diary.app.event.friendrequest.FriendRequestNotification;
import com.cheolhyeon.diary.app.exception.friendrequest.FriendRequestErrorStatus;
import com.cheolhyeon.diary.app.exception.friendrequest.FriendRequestException;
import com.cheolhyeon.diary.app.exception.session.UserException;
import com.cheolhyeon.diary.app.exception.sharecode.ShareCodeException;
import com.cheolhyeon.diary.auth.entity.AuthSession;
import com.cheolhyeon.diary.auth.entity.User;
import com.cheolhyeon.diary.auth.repository.UserRepository;
import com.cheolhyeon.diary.auth.session.SessionRepository;
import com.cheolhyeon.diary.friendrequest.dto.request.FriendRequestActionRequest;
import com.cheolhyeon.diary.friendrequest.dto.response.SearchShareCodeOwnerResponse;
import com.cheolhyeon.diary.friendrequest.entity.FriendRequest;
import com.cheolhyeon.diary.friendrequest.enums.FriendRequestStatus;
import com.cheolhyeon.diary.friendrequest.repository.FriendRequestRepository;
import com.cheolhyeon.diary.sharecode.entity.ShareCode;
import com.cheolhyeon.diary.sharecode.enums.ShareCodeStatus;
import com.cheolhyeon.diary.sharecode.repository.ShareCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FriendRequestService 단위 테스트")
class FriendRequestServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private ShareCodeRepository shareCodeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private com.cheolhyeon.diary.app.util.HashCodeGenerator hashCodeGenerator;

    @InjectMocks
    private FriendRequestService friendRequestService;

    private ShareCode testShareCode;
    private User shareCodeOwner;
    private User requesterUser;
    private AuthSession ownerSession;
    private Long ownerId;
    private Long requesterId;
    private String hashShareCode;
    private String plainShareCode;

    @BeforeEach
    void setUp() {
        ownerId = 100L;
        requesterId = 200L;
        plainShareCode = "PLAIN_CODE_123";
        hashShareCode = "HASH_CODE_ABC123";

        testShareCode = ShareCode.builder()
                .userId(ownerId)
                .codePlain(plainShareCode)
                .codeHash(hashShareCode)
                .status(ShareCodeStatus.ACTIVE)
                .build();

        shareCodeOwner = new User(
                ownerId,
                "owner@test.com",
                "010-0000-0000",
                "공유코드소유자",
                com.cheolhyeon.diary.auth.enums.UserActiveStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        requesterUser = new User(
                requesterId,
                "requester@test.com",
                "010-1111-1111",
                "요청자유저",
                com.cheolhyeon.diary.auth.enums.UserActiveStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        ownerSession = new AuthSession(
                "owner-session-id",
                ownerId,
                "hash_rt",
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(5),
                "User-Agent",
                "127.0.0.1"
        );
    }

    @Test
    @DisplayName("ShareCode로 소유자 검색 성공")
    void searchOwnerByShareCode_Success() {
        // Given
        given(hashCodeGenerator.generateShareCodeHash(plainShareCode)).willReturn(hashShareCode);
        given(shareCodeRepository.findShareCodeByHashCode(hashShareCode)).willReturn(Optional.of(testShareCode));
        given(userRepository.findById(ownerId)).willReturn(Optional.of(shareCodeOwner));

        // When
        SearchShareCodeOwnerResponse response = friendRequestService.searchOwnerByShareCode(plainShareCode);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOwnerDisplayName()).isEqualTo("공유코드소유자");
        assertThat(response.getShareCodeHash()).isNotNull();

        verify(hashCodeGenerator).generateShareCodeHash(plainShareCode);
        verify(shareCodeRepository).findShareCodeByHashCode(hashShareCode);
        verify(userRepository).findById(ownerId);
    }

    @Test
    @DisplayName("ShareCode로 소유자 검색 실패 - ShareCode가 존재하지 않음")
    void searchOwnerByShareCode_Fail_ShareCodeNotFound() {
        // Given
        given(hashCodeGenerator.generateShareCodeHash(plainShareCode)).willReturn(hashShareCode);
        given(shareCodeRepository.findShareCodeByHashCode(hashShareCode)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> friendRequestService.searchOwnerByShareCode(plainShareCode))
                .isInstanceOf(ShareCodeException.class);

        verify(hashCodeGenerator).generateShareCodeHash(plainShareCode);
        verify(shareCodeRepository).findShareCodeByHashCode(hashShareCode);
    }

    @Test
    @DisplayName("ShareCode로 소유자 검색 실패 - 사용자가 존재하지 않음")
    void searchOwnerByShareCode_Fail_UserNotFound() {
        // Given
        given(hashCodeGenerator.generateShareCodeHash(plainShareCode)).willReturn(hashShareCode);
        given(shareCodeRepository.findShareCodeByHashCode(hashShareCode)).willReturn(Optional.of(testShareCode));
        given(userRepository.findById(ownerId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> friendRequestService.searchOwnerByShareCode(plainShareCode))
                .isInstanceOf(UserException.class);

        verify(hashCodeGenerator).generateShareCodeHash(plainShareCode);
        verify(shareCodeRepository).findShareCodeByHashCode(hashShareCode);
        verify(userRepository).findById(ownerId);
    }

    @Test
    @DisplayName("친구 요청 생성 성공")
    void requestFriendRequest_Success() {
        // Given
        given(shareCodeRepository.findShareCodeByHashCode(hashShareCode)).willReturn(Optional.of(testShareCode));
        given(sessionRepository.findByUserId(ownerId)).willReturn(Optional.of(ownerSession));
        given(userRepository.findById(requesterId)).willReturn(Optional.of(requesterUser));
        given(friendRequestRepository.save(any(FriendRequest.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        friendRequestService.requestFriendRequest(hashShareCode, requesterId);

        // Then
        ArgumentCaptor<FriendRequest> friendRequestCaptor = ArgumentCaptor.forClass(FriendRequest.class);
        verify(friendRequestRepository).save(friendRequestCaptor.capture());

        FriendRequest savedRequest = friendRequestCaptor.getValue();
        assertThat(savedRequest.getId()).isNotNull();
        assertThat(savedRequest.getOwnerUserId()).isEqualTo(ownerId);
        assertThat(savedRequest.getRequesterUserId()).isEqualTo(requesterId);
        assertThat(savedRequest.getHashShareCode()).isEqualTo(hashShareCode);
        assertThat(savedRequest.getStatus()).isEqualTo(FriendRequestStatus.PENDING);
        assertThat(savedRequest.getCreatedAt()).isNotNull();
        assertThat(savedRequest.getDecidedAt()).isNull();

        verify(shareCodeRepository).findShareCodeByHashCode(hashShareCode);
        verify(sessionRepository).findByUserId(ownerId);
        verify(userRepository).findById(requesterId);
        verify(eventPublisher).publishEvent(any(FriendRequestNotification.class));
    }

    @Test
    @DisplayName("친구 요청 생성 실패 - ShareCode가 존재하지 않음")
    void requestFriendRequest_Fail_ShareCodeNotFound() {
        // Given
        given(shareCodeRepository.findShareCodeByHashCode(hashShareCode)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> friendRequestService.requestFriendRequest(hashShareCode, requesterId))
                .isInstanceOf(ShareCodeException.class);

        verify(shareCodeRepository).findShareCodeByHashCode(hashShareCode);
    }

    @Test
    @DisplayName("친구 요청 생성 실패 - 대상자 세션이 존재하지 않음")
    void requestFriendRequest_Fail_OwnerSessionNotFound() {
        // Given
        given(shareCodeRepository.findShareCodeByHashCode(hashShareCode)).willReturn(Optional.of(testShareCode));
        given(sessionRepository.findByUserId(ownerId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> friendRequestService.requestFriendRequest(hashShareCode, requesterId))
                .isInstanceOf(UserException.class);

        verify(shareCodeRepository).findShareCodeByHashCode(hashShareCode);
        verify(sessionRepository).findByUserId(ownerId);
    }

    @Test
    @DisplayName("친구 요청 생성 실패 - 요청자가 존재하지 않음")
    void requestFriendRequest_Fail_RequesterNotFound() {
        // Given
        given(shareCodeRepository.findShareCodeByHashCode(hashShareCode)).willReturn(Optional.of(testShareCode));
        given(sessionRepository.findByUserId(ownerId)).willReturn(Optional.of(ownerSession));
        given(userRepository.findById(requesterId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> friendRequestService.requestFriendRequest(hashShareCode, requesterId))
                .isInstanceOf(UserException.class);

        verify(shareCodeRepository).findShareCodeByHashCode(hashShareCode);
        verify(sessionRepository).findByUserId(ownerId);
        verify(userRepository).findById(requesterId);
    }

    @Test
    @DisplayName("친구 요청 생성 - 이벤트 발행 확인")
    void requestFriendRequest_EventPublished() {
        // Given
        given(shareCodeRepository.findShareCodeByHashCode(hashShareCode)).willReturn(Optional.of(testShareCode));
        given(sessionRepository.findByUserId(ownerId)).willReturn(Optional.of(ownerSession));
        given(userRepository.findById(requesterId)).willReturn(Optional.of(requesterUser));
        given(friendRequestRepository.save(any(FriendRequest.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        friendRequestService.requestFriendRequest(hashShareCode, requesterId);

        // Then
        verify(shareCodeRepository).findShareCodeByHashCode(hashShareCode);
        verify(sessionRepository).findByUserId(ownerId);
        verify(userRepository).findById(requesterId);
        verify(friendRequestRepository).save(any(FriendRequest.class));
        verify(eventPublisher).publishEvent(any(FriendRequestNotification.class));
    }

    @Test
    @DisplayName("친구 요청 수락 성공")
    void decide_Success_Accept() {
        // Given
        String requestId = "REQ_ACCEPT_123";
        Long userId = 1L;
        FriendRequestActionRequest actionRequest = FriendRequestActionRequest.builder()
                .id(requestId)
                .decide(FriendRequestStatus.ACCEPTED.name())
                .build();

        FriendRequest pendingRequest = FriendRequest.builder()
                .id(requestId)
                .ownerUserId(ownerId)
                .requesterUserId(requesterId)
                .hashShareCode(hashShareCode)
                .status(FriendRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .decidedAt(null)
                .build();

        given(friendRequestRepository.findPendingById(requestId, FriendRequestStatus.PENDING.name()))
                .willReturn(Optional.of(pendingRequest));

        // When
        FriendRequestStatus result = friendRequestService.decide(userId, actionRequest);

        // Then
        assertThat(result).isEqualTo(FriendRequestStatus.ACCEPTED);
        verify(friendRequestRepository).findPendingById(requestId, FriendRequestStatus.PENDING.name());
        verify(friendRequestRepository).updateStatusByUserAction(
                anyString(), anyLong(), anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("친구 요청 거절 성공")
    void decide_Success_Decline() {
        // Given
        String requestId = "REQ_DECLINE_456";
        Long userId = 1L;
        FriendRequestActionRequest actionRequest = FriendRequestActionRequest.builder()
                .id(requestId)
                .decide(FriendRequestStatus.DECLINED.name())
                .build();

        FriendRequest pendingRequest = FriendRequest.builder()
                .id(requestId)
                .ownerUserId(ownerId)
                .requesterUserId(requesterId)
                .hashShareCode(hashShareCode)
                .status(FriendRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .decidedAt(null)
                .build();

        given(friendRequestRepository.findPendingById(requestId, FriendRequestStatus.PENDING.name()))
                .willReturn(Optional.of(pendingRequest));

        // When
        FriendRequestStatus result = friendRequestService.decide(userId, actionRequest);

        // Then
        assertThat(result).isEqualTo(FriendRequestStatus.DECLINED);
        verify(friendRequestRepository).findPendingById(requestId, FriendRequestStatus.PENDING.name());
        verify(friendRequestRepository).updateStatusByUserAction(
                anyString(), anyLong(), anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("친구 요청 처리 실패 - 이미 처리된 요청")
    void decide_Fail_AlreadyDecided() {
        // Given
        String requestId = "REQ_ALREADY_DECIDED";
        Long userId = 1L;
        FriendRequestActionRequest actionRequest = FriendRequestActionRequest.builder()
                .id(requestId)
                .decide(FriendRequestStatus.ACCEPTED.name())
                .build();

        given(friendRequestRepository.findPendingById(requestId, FriendRequestStatus.PENDING.name()))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> friendRequestService.decide(userId, actionRequest))
                .isInstanceOf(FriendRequestException.class)
                .hasFieldOrPropertyWithValue("errorStatus", FriendRequestErrorStatus.ALREADY_DECIDED_REQUEST);

        verify(friendRequestRepository).findPendingById(requestId, FriendRequestStatus.PENDING.name());
    }

    @Test
    @DisplayName("친구 요청 처리 - 다양한 사용자로 요청")
    void decide_WithDifferentUsers() {
        // Given
        String requestId = "REQ_DIFF_USER";
        Long differentUserId = 999L;
        FriendRequestActionRequest actionRequest = FriendRequestActionRequest.builder()
                .id(requestId)
                .decide(FriendRequestStatus.ACCEPTED.name())
                .build();

        FriendRequest pendingRequest = FriendRequest.builder()
                .id(requestId)
                .ownerUserId(ownerId)
                .requesterUserId(requesterId)
                .hashShareCode(hashShareCode)
                .status(FriendRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .decidedAt(null)
                .build();

        given(friendRequestRepository.findPendingById(requestId, FriendRequestStatus.PENDING.name()))
                .willReturn(Optional.of(pendingRequest));

        // When
        FriendRequestStatus result = friendRequestService.decide(differentUserId, actionRequest);

        // Then
        assertThat(result).isEqualTo(FriendRequestStatus.ACCEPTED);
        verify(friendRequestRepository).findPendingById(requestId, FriendRequestStatus.PENDING.name());
        verify(friendRequestRepository).updateStatusByUserAction(
                anyString(), anyLong(), anyString(), any(LocalDateTime.class));
    }
}

