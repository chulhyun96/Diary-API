package com.cheolhyeon.diary.sharecode.dto.request;

import com.cheolhyeon.diary.sharecode.entity.ShareCode;
import com.cheolhyeon.diary.sharecode.enums.ShareCodeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ShareCodeCreateRequest {
    @Size(min = 1, max = 30)
    @NotBlank(message = "코드는 필수입니다")
    private String code;

    public ShareCode toEntity(Long userId, String codeHash) {
        return ShareCode.builder()
                .userId(userId)
                .codePlain(code)
                .codeHash(codeHash)
                .status(ShareCodeStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();
    }
}
