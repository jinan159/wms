package com.teamfresh.wms.application.dto;

import com.teamfresh.wms.domain.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;

public record OrderRegisterRequestDto(
    @NotEmpty
    List<@Valid OrderItemDto> orderItems,

    @NotNull
    @Valid OrdererDto orderer,

    @NotNull
    Order.ChannelType channelType
) {
    public Order toDomain() {
        var orderDomain = Order.createRequestedOrder()
            .channelType(Order.ChannelType.TEAM_FRESH_MALL) // 가상의 주문 채널값을 넣어줌
            .customerName(orderer.ordererName())
            .address(orderer().address().toDomain())
            .orderedAt(ZonedDateTime.now())
            .build();

        var orderItemDomains = orderItems.stream()
            .map(OrderItemDto::toDomain)
            .toList();

        orderDomain.registerOrderItems(orderItemDomains);

        return orderDomain;
    }
}
