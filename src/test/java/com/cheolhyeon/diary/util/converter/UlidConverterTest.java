package com.cheolhyeon.diary.util.converter;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UlidConverterTest {

    @Test
    void ulidToBinary() {
        System.out.println("UlidConverter.generateUlid() = " + UlidConverter.generateUlid());
        byte[] bytes = UlidConverter.ulidToBinary(UlidConverter.generateUlid());
        System.out.println("UlidConverter.binaryToUlid(bytes) = " + UlidConverter.binaryToUlid(bytes));
        System.out.println("bytes = " + Arrays.toString(bytes));

    }
}