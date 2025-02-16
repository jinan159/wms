package com.teamfresh.wms.application.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record OrderUploadRequestDto(
    @NotEmpty
    List<OrderCreateRequestDto> orderCreateRequests,
    @NotEmpty
    Map<UUID, Long> productQuantityMap
) {
}
