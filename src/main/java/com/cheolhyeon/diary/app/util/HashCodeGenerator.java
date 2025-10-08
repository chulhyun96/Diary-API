package com.cheolhyeon.diary.app.util;

import com.cheolhyeon.diary.app.exception.hashcode.GenerationHashCodeErrorStatus;
import com.cheolhyeon.diary.app.exception.hashcode.GenerationHashCodeException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public abstract class HashCodeGenerator {

    private HashCodeGenerator() {
        throw new AssertionError("No " + HashCodeGenerator.class.getSimpleName() + " instances");
    }

    public static String generateShareCodeHash(String code) {
        final String rtHmacSecret = "Rm3RDpZJc3lyOFe5DeUWPXyMPAbsEgYWVY5Qtucxpcg=";
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
