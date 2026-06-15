package com.grim.contextos.common.response;

import java.util.List;

public record PageResponse<T>(
        List<T> context,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> of(
            org.springframework.data.domain.Page<T>
            page
    ){
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
