package com.cheolhyeon.diary.util.converter;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;

/**
 * ULID와 BINARY(16) 간의 변환을 위한 유틸리티 클래스
 * 
 * ULID: 26자 Base32 문자열 (예: 01HNGXKNGP-9M5X7M5X7M5X7)
 * BINARY(16): 16바이트 바이너리 데이터
 */
public abstract class UlidConverter {

    private UlidConverter() {
        throw new IllegalStateException("Utility class");
    }

    public static String binaryToUlid(byte[] binary) {
        if (binary == null || binary.length != 16) {
            throw new IllegalArgumentException("BINARY는 16바이트여야 합니다: " + binary.length);
        }
        
        try {
            Ulid ulid = Ulid.from(binary);
            return ulid.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 BINARY 형식", e);
        }
    }
    
    public static byte[] generateUlid() {
        return UlidCreator.getUlid().toBytes();
    }
}
