package com.cheolhyeon.diary.app.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ToString
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private int accessTokenExpiration;
    private int refreshTokenExpiration;
    private String iss;
    private String aud;
    private int rtLengthBytes;
    private String rtHmacSecret;
}

