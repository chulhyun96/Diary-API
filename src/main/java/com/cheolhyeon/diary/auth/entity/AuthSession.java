package com.cheolhyeon.diary.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auth_session")
public class AuthSession {
    @Id
    private String sessionId;
    private Long userId;
    private String rtHashCurrent;
    private String rtHashPrev;
    private LocalDateTime createdAt;
    private LocalDateTime lastRefreshAt;
    @Column(name = "expires_at")
    private LocalDateTime expiredAt;
    private String userAgent;
    private String ipAddr;

    public void refresh(String newHashRT, String prevHashRT) {
        this.rtHashCurrent = newHashRT;
        this.rtHashPrev = prevHashRT;
        this.lastRefreshAt = LocalDateTime.now();
    }
}
