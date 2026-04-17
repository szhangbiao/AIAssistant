package cn.booslink.llm.common.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {

    @SerializedName("err_code")
    private int code;
    @SerializedName("err_msg")
    private String message;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return code == 200;
    }
}
