package com.cheolhyeon.diary.app.util;


import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;

public abstract class UlidGenerator {

    private UlidGenerator() {
        throw new AssertionError("No " +UlidGenerator.class.getSimpleName() + " instances");
    }
    
    public static byte[] generatorUlid() {
        return UlidCreator.getUlid().toBytes();
    }
    
    public static String ulidBytesToString(byte[] ulidBytes) {
        return Ulid.from(ulidBytes).toString();
    }

    public static String generatorUlidForSession() {
        return UlidCreator.getUlid().toString();
    }
}
