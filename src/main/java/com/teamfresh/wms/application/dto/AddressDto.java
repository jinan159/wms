package com.teamfresh.wms.application.dto;

import com.teamfresh.wms.domain.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressDto(
    @Pattern(regexp = "\\d{5,6}", message = "우편번호는 5~6자리 숫자를 입력해야 합니다.")
    String postalCode,

    @NotBlank
    String city,

    @NotBlank
    String district,

    @NotBlank
    String streetAddress,

    String addressDetail
) {
    public Address toDomain() {
        return Address.builder()
            .postalCode(postalCode)
            .city(city)
            .district(district)
            .streetAddress(streetAddress)
            .addressDetail(addressDetail)
            .build();
    }
}