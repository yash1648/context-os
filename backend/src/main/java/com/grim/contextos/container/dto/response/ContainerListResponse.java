package com.grim.contextos.container.dto.response;

import java.util.List;

public record ContainerListResponse(
    List<ContainerResponse> containers,
    long total,
    long running,
    long stopped,
    long failed
) {
    public static ContainerListResponse of(List<ContainerResponse> containers,
                                            long total, long running,
                                            long stopped, long failed) {
        return new ContainerListResponse(containers, total, running, stopped, failed);
    }
}
