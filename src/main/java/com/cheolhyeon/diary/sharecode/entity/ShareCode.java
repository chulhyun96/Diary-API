package com.cheolhyeon.diary.sharecode.entity;

import com.cheolhyeon.diary.sharecode.enums.ShareCodeStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareCode {
    @Id
    private Long userId;
    private String codePlain;
    private String codeHash;

    @Enumerated(EnumType.STRING)
    private ShareCodeStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void updateShareCode(String hashCode, String codePlain) {
        this.codePlain = codePlain;
        this.codeHash = hashCode;
        this.updatedAt = LocalDateTime.now();
    }

    public void revokedStatus(ShareCodeStatus shareCodeStatus) {
        this.status = shareCodeStatus;
    }
}
