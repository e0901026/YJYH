package com.yjyh.phoneloan.backend.common;

public final class Imei {
    private Imei() {
    }

    public static boolean isValid(String value) {
        return value != null && value.matches("\\d{15}");
    }
}
