package com.cheolhyeon.diary.app.util;

import com.cheolhyeon.diary.app.exception.hashcode.GenerationHashCodeErrorStatus;
import com.cheolhyeon.diary.app.exception.hashcode.GenerationHashCodeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class HashCodeGenerator {
    private final String rtHmacSecret;

    public HashCodeGenerator(@Value("${jwt.secret}") String rtHmacSecret) {
        this.rtHmacSecret = rtHmacSecret;
    }

    public String generateShareCodeHash(String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            byte[] secretKey = Base64.getDecoder().decode(rtHmacSecret);
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] h = mac.doFinal(code.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(h)
                    .replace('+','-')
                    .replace('/','_')
                    .replace("=", "");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new GenerationHashCodeException(GenerationHashCodeErrorStatus.GENERATE_FAILED_HASH_CODE);
        }
    }
}
