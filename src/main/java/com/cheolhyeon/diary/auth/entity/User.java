package com.cheolhyeon.diary.auth.entity;

import com.cheolhyeon.diary.auth.dto.response.KakaoUserInfoResponse;
import com.cheolhyeon.diary.auth.enums.UserActiveStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    @Id
    @Column(name = "kakao_id")
    private Long kakaoId;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "display_name")
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserActiveStatus status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;


    public static User createUser(KakaoUserInfoResponse me) {
        User newUser = new User();
        newUser.kakaoId = me.getId();
        newUser.email = "";
        newUser.phone = "";
        newUser.displayName = me.getKakao_account().getProfile().getNickname();
        newUser.status = UserActiveStatus.ACTIVE;
        newUser.lastLoginAt = LocalDateTime.now();
        newUser.createdAt = LocalDateTime.now();
        return newUser;
    }
    
    public void updateLastLoginTime() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
