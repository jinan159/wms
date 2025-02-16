package com.teamfresh.wms.application.dto;

import java.io.InputStream;

public record OrderUploadRequestDto(
    InputStream inputStream
) {
}
