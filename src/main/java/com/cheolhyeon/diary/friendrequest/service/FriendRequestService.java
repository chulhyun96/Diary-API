package com.cheolhyeon.diary.friendrequest.service;

import com.cheolhyeon.diary.app.event.friendrequest.FriendRequestNotification;
import com.cheolhyeon.diary.app.exception.friendrequest.FriendRequestErrorStatus;
import com.cheolhyeon.diary.app.exception.friendrequest.FriendRequestException;
import com.cheolhyeon.diary.app.exception.session.UserErrorStatus;
import com.cheolhyeon.diary.app.exception.session.UserException;
import com.cheolhyeon.diary.app.exception.sharecode.ShareCodeErrorStatus;
import com.cheolhyeon.diary.app.exception.sharecode.ShareCodeException;
import com.cheolhyeon.diary.app.util.HashCodeGenerator;
import com.cheolhyeon.diary.app.util.UlidGenerator;
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
import com.cheolhyeon.diary.sharecode.repository.ShareCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendRequestService {
    private final ApplicationEventPublisher eventPublisher;
    private final FriendRequestRepository friendRequestRepository;
    private final ShareCodeRepository shareCodeRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final HashCodeGenerator hashCodeGenerator;

    public SearchShareCodeOwnerResponse searchOwnerByShareCode(String plainShareCode) {
        String hashShareCode = hashCodeGenerator.generateShareCodeHash(plainShareCode);
        ShareCode shareCode = shareCodeRepository.findShareCodeByHashCode(hashShareCode)
                .orElseThrow(() -> new ShareCodeException(ShareCodeErrorStatus.NOT_FOUND));
        User shareCodeOwner = userRepository.findById(shareCode.getUserId())
                .orElseThrow(() -> new UserException(UserErrorStatus.NOT_FOUND));
        return SearchShareCodeOwnerResponse.toResponse(shareCodeOwner, hashShareCode);
    }

    @Transactional
    public void requestFriendRequest(String hashShareCode, Long userId) {
        // 대상자
        ShareCode shareCode = shareCodeRepository.findShareCodeByHashCode(hashShareCode)
                .orElseThrow(() -> new ShareCodeException(ShareCodeErrorStatus.NOT_FOUND));
        Long shareCodeOwnerUserId = shareCode.getUserId();
        AuthSession codeOwnerSession = sessionRepository.findByUserId(shareCodeOwnerUserId)
                .orElseThrow(() -> new UserException(UserErrorStatus.NOT_FOUND));


        //요청자
        User requesterUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorStatus.NOT_FOUND));
        Long requesterUserId = requesterUser.getUserId();

        String id = UlidGenerator.generatorUlidAsString();
        friendRequestRepository.save(
                FriendRequest.builder()
                        .id(id)
                        .ownerUserId(shareCodeOwnerUserId)
                        .requesterUserId(requesterUserId)
                        .hashShareCode(hashShareCode)
                        .status(FriendRequestStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .decidedAt(null)
                        .build());
        eventPublisher.publishEvent(new FriendRequestNotification(
                codeOwnerSession.getSessionId(), id, shareCodeOwnerUserId, requesterUserId, requesterUser.getDisplayName()));
    }

    @Transactional
    public FriendRequestStatus decide(Long userId, FriendRequestActionRequest decideRequest) {
        friendRequestRepository.findPendingById(decideRequest.getId(), FriendRequestStatus.PENDING.name())
                .orElseThrow(() -> new FriendRequestException(FriendRequestErrorStatus.ALREADY_DECIDED_REQUEST));
        if (decideRequest.getDecide().equals(FriendRequestStatus.ACCEPTED.name())) {
            friendRequestRepository.updateStatusByUserAction(
                    decideRequest.getId(),
                    userId,
                    FriendRequestStatus.ACCEPTED.name(),
                    LocalDateTime.now());
            // TODO 현재 유저에 대한 친구 목록에 추가 -> 현재 세션의 친구 목록을 관리할 테이블
            return FriendRequestStatus.ACCEPTED;
        }
        friendRequestRepository.updateStatusByUserAction(
                decideRequest.getId(),
                userId,
                FriendRequestStatus.DECLINED.name(),
                LocalDateTime.now());
        return FriendRequestStatus.DECLINED;
    }
}
