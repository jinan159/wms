package com.teamfresh.wms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;

@Embeddable
public record Address(
    @Column(name = "postal_code", nullable = false)
    String postalCode,
    @Column(name = "city", nullable = false)
    String city,
    @Column(name = "district", nullable = false)
    String district,
    @Column(name = "street_address", nullable = false)
    String streetAddress,
    @Column(name = "address_detail")
    String addressDetail
) {
    @Builder
    public Address {
    }
}