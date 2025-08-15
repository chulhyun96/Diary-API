package com.cheolhyeon.diary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private byte[] id;

    @Column(unique = true, nullable = false, name = "oauth2_id")
    private String oauth2Id;

    @Column(nullable = false)
    private String nickname;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static User createUser(byte[] id, String oauth2Id, String nickname) {
        User user = new User();
        user.id = id;
        user.oauth2Id = oauth2Id;
        user.nickname = nickname;
        return user;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}