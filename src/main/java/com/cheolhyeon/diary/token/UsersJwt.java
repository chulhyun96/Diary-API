package com.cheolhyeon.diary.token;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users_jwt")
public class UsersJwt {
    @Id
    private Long usersId;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expireAt;
    private LocalDateTime createAt;
}
