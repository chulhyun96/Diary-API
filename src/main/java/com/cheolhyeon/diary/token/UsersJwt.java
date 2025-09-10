package com.cheolhyeon.diary.token;

import com.cheolhyeon.diary.token.dto.request.JwtRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
    private LocalDateTime createdAt;
    private LocalDateTime refreshExpiresAt;

    public static UsersJwt create(JwtRequest jwt) {
        LocalDateTime expireLdt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(jwt.getRefreshExpireDate()),
                ZoneId.systemDefault()
        );
        LocalDateTime createLdt = jwt.getCreateDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return new UsersJwt(
                jwt.getUserId(),
                jwt.getAccessToken(),
                jwt.getRefreshToken(),
                createLdt,
                expireLdt);
    }
}
