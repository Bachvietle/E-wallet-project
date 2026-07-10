package com.BachLe.ewallet.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApiResponse <T>{

    private String message;

    private T data;

    private boolean success;

    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(message, data, true);
    }

    public static <T> ApiResponse<T> error(String message){
        return new ApiResponse<>(message, null, false);
    }
}
