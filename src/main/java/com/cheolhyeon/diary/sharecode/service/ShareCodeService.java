package com.cheolhyeon.diary.sharecode.service;

import com.cheolhyeon.diary.app.exception.hashcode.GenerationHashCodeErrorStatus;
import com.cheolhyeon.diary.app.exception.hashcode.GenerationHashCodeException;
import com.cheolhyeon.diary.app.exception.sharecode.ShareCodeErrorStatus;
import com.cheolhyeon.diary.app.exception.sharecode.ShareCodeException;
import com.cheolhyeon.diary.app.properties.JwtProperties;
import com.cheolhyeon.diary.sharecode.dto.ShareCodeCreateResponse;
import com.cheolhyeon.diary.sharecode.dto.request.ShareCodeCreateRequest;
import com.cheolhyeon.diary.sharecode.entity.ShareCode;
import com.cheolhyeon.diary.sharecode.enums.ShareCodeStatus;
import com.cheolhyeon.diary.sharecode.repository.ShareCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShareCodeService {
    private final ShareCodeRepository shareCodeRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public ShareCodeCreateResponse create(Long userId, ShareCodeCreateRequest request) {
        shareCodeRepository.findShareCodeById(userId)
                .ifPresent(shareCode -> {
                    throw new ShareCodeException(ShareCodeErrorStatus.ONLY_SINGLE_SHARE_CODE);
                });

        String codeHash = generateShareCode(request.getCode());
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
        String hashCode = generateShareCode(codePlain);
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

    private String generateShareCode(String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            byte[] secretKey = Base64.getDecoder().decode(jwtProperties.getRtHmacSecret());
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] h = mac.doFinal(code.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().withoutPadding().encodeToString(h);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new GenerationHashCodeException(GenerationHashCodeErrorStatus.GENERATE_FAILED_HASH_CODE);
        }
    }
}
