package com.example.fitnessplatformbackend.common;

public class ApiException extends RuntimeException {
    private final int code;

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }
    public int getCode() { return code; }

    public static ApiException badRequest(String msg) { return new ApiException(40001, msg); }
    public static ApiException unauthorized(String msg) { return new ApiException(40101, msg); }
    public static ApiException forbidden(String msg) { return new ApiException(40301, msg); }
    public static ApiException notFound(String msg) { return new ApiException(40401, msg); }
    public static ApiException conflict(String msg) { return new ApiException(40901, msg); }
}
