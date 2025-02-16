package com.teamfresh.wms.presentation.dto;

import java.util.List;
import java.util.UUID;

public record OrderUploadResponseDto(
    List<UUID> orderIds
) {
}
