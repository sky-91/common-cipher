package com.example.commoncipher.result;

import java.io.Serializable;

public class ExampleCommonResult<T> implements Serializable {

    private static final long serialVersionUID = 9191892693219217387L;
    public static final String RESP_CODE_SUCCESS = "00000000";
    public static final String RESP_MSG_SUCCESS = "success";
    private String code;
    private boolean success;
    private String message;
    private T data;

    public ExampleCommonResult() {
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static <T> ExampleCommonResult<T> success(T data) {
        ExampleCommonResult<T> result = new ExampleCommonResult();
        result.setCode("00000000");
        result.setMessage("success");
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    public static <T> ExampleCommonResult<T> fail(String code, String message, T data) {
        ExampleCommonResult<T> result = new ExampleCommonResult();
        result.setCode(code);
        result.setData(data);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    public static <T> ExampleCommonResult<T> fail(String code, String message) {
        ExampleCommonResult<T> result = new ExampleCommonResult();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        result.setData(null);
        return result;
    }
}
