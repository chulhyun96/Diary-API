package com.cheolhyeon.diary.app.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HashCodeGeneratorTest {

    @Autowired
    private HashCodeGenerator hashCodeGenerator;

    @Test
    @DisplayName("해시 코드 생성 테스트 - 동일한 입력은 동일한 해시를 생성해야 함")
    void testGenerateShareCodeHash_SameInputProducesSameHash() {
        // given
        String testCode = "testCode123";

        // when
        String hash1 = hashCodeGenerator.generateShareCodeHash(testCode);
        String hash2 = hashCodeGenerator.generateShareCodeHash(testCode);

        // then
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("해시 코드 생성 테스트 - 다른 입력은 다른 해시를 생성해야 함")
    void testGenerateShareCodeHash_DifferentInputsProduceDifferentHashes() {
        // given
        String testCode1 = "testCode123";
        String testCode2 = "testCode456";

        // when
        String hash1 = hashCodeGenerator.generateShareCodeHash(testCode1);
        String hash2 = hashCodeGenerator.generateShareCodeHash(testCode2);

        // then
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("해시 코드 생성 테스트 - URL safe 문자만 포함해야 함")
    void testGenerateShareCodeHash_IsUrlSafe() {
        // given
        String testCode = "testCode123";

        // when
        String hash = hashCodeGenerator.generateShareCodeHash(testCode);

        // then
        assertNotNull(hash);
        assertFalse(hash.contains("+"));
        assertFalse(hash.contains("/"));
        assertFalse(hash.contains("="));
    }
}