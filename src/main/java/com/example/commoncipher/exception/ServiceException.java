package com.example.commoncipher.exception;

public class ServiceException extends Exception {
    private static final long serialVersionUID = -1219262335729891920L;
    private String code;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String code, String message) {
        this(message);
        this.code = code;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
