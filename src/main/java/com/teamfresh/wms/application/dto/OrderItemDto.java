package com.teamfresh.wms.application.dto;

import com.teamfresh.wms.domain.OrderItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OrderItemDto(
    @NotNull
    UUID productId,

    @Min(value = 1, message = "quantity must be at least 1")
    int quantity
) {
    public OrderItem toDomain() {
        return OrderItem.createNew()
            .productId(productId)
            .quantity(quantity)
            .build();
    }
}