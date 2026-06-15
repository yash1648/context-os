package com.grim.contextos.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String timestamp

) {
    public static <T> ApiResponse<T> ok(T data){
        return new ApiResponse<>(true,data, Instant.now().toString());
    }

}

