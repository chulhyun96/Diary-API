package com.cheolhyeon.diary.sharecode.service;

import com.cheolhyeon.diary.app.exception.sharecode.ShareCodeErrorStatus;
import com.cheolhyeon.diary.app.exception.sharecode.ShareCodeException;
import com.cheolhyeon.diary.app.util.HashCodeGenerator;
import com.cheolhyeon.diary.sharecode.dto.ShareCodeCreateResponse;
import com.cheolhyeon.diary.sharecode.dto.request.ShareCodeCreateRequest;
import com.cheolhyeon.diary.sharecode.entity.ShareCode;
import com.cheolhyeon.diary.sharecode.enums.ShareCodeStatus;
import com.cheolhyeon.diary.sharecode.repository.ShareCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShareCodeService {
    private final ShareCodeRepository shareCodeRepository;


    @Transactional
    public ShareCodeCreateResponse create(Long userId, ShareCodeCreateRequest request) {
        shareCodeRepository.findShareCodeById(userId)
                .ifPresent(shareCode -> {
                    throw new ShareCodeException(ShareCodeErrorStatus.ONLY_SINGLE_SHARE_CODE);
                });

        String codeHash = generateShareCodeHash(request.getCode());
        ShareCode entity = request.toEntity(userId, codeHash);
        shareCodeRepository.save(entity);
        return ShareCodeCreateResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .codePlain(request.getCode())
                .message("공유 코드가 성공적으로 생성되었습니다.")
                .build();
    }

    @Transactional
    public void updateShareCode(Long userId, ShareCodeCreateRequest request) {
        ShareCode shareCode = shareCodeRepository.findShareCodeById(userId)
                .orElseThrow(() -> new ShareCodeException(ShareCodeErrorStatus.NOT_FOUND));
        String codePlain = request.getCode();
        String hashCode = generateShareCodeHash(codePlain);
        shareCode.updateShareCode(hashCode, codePlain);
    }

    public String readMyShareCode(Long userId) {
        return shareCodeRepository.findShareCodeById(userId)
                .map(ShareCode::getCodePlain)
                .orElse("");
    }

    @Transactional
    public void deleteMyShareCode(Long userId) {
        ShareCode shareCode = shareCodeRepository.findShareCodeById(userId)
                .orElseThrow(() -> new ShareCodeException(ShareCodeErrorStatus.NOT_FOUND));
        shareCode.revokedStatus(ShareCodeStatus.REVOKED);
    }

    private String generateShareCodeHash(String code) {
        return HashCodeGenerator.generateShareCodeHash(code);
    }
}
