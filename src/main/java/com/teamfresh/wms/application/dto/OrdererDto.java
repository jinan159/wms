package com.teamfresh.wms.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrdererDto(
    @NotBlank
    String ordererName,

    @NotNull
    AddressDto addressDto
) {
}
