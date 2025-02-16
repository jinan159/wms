package com.teamfresh.wms.application.service;

import com.teamfresh.wms.application.dto.AddressDto;
import com.teamfresh.wms.application.dto.OrderCreateRequestDto;
import com.teamfresh.wms.application.dto.OrderItemDto;
import com.teamfresh.wms.application.dto.OrdererDto;
import com.teamfresh.wms.domain.order.Order;
import com.teamfresh.wms.infra.document.SpreadSheetDocument;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Builder;

public class SpreadSheetOrderCreateRequestMapper {
    public List<OrderCreateRequestDto> map(SpreadSheetDocument document) {
        var firstSheet = document.get(0);

        var uploadedRows = firstSheet.rows()
            .stream()
            .skip(1) // 헤더 제외
            .map(row -> UploadedOrderRow.builder()
                .orderGroupCode(row.get(0).value())
                .channelType(row.get(1).value())
                .ordererName(row.get(2).value())
                .postalCode(row.get(3).value())
                .city(row.get(4).value())
                .district(row.get(5).value())
                .streetAddress(row.get(6).value())
                .detailAddress(row.get(7).value())
                .productId(row.get(8).value())
                .quantity(row.get(9).value())
                .build()
            )
            .toList();

        var groupedRows = uploadedRows.stream()
            .collect(
                Collectors.groupingBy(
                    row -> row.orderGroupCode,
                    Collectors.mapping(
                        row -> row,
                        Collectors.toList()
                    )
                )
            );

        return groupedRows.values().stream()
            .map(rows -> {
                var orderItems = rows.stream()
                    .map(row -> new OrderItemDto(
                        UUID.fromString(row.productId),
                        Integer.parseInt(row.quantity)
                    ))
                    .toList();

                var firstRow = rows.stream().findFirst()
                    .orElseThrow();

                return OrderCreateRequestDto.builder()
                    .orderItems(orderItems)
                    .channelType(
                        Order.ChannelType.valueOf(firstRow.channelType)
                    )
                    .orderer(
                        new OrdererDto(
                            firstRow.ordererName,
                            new AddressDto(
                                firstRow.postalCode,
                                firstRow.city,
                                firstRow.district,
                                firstRow.streetAddress,
                                firstRow.detailAddress
                            )
                        )
                    )
                    .build();
            })
            .toList();
    }

    @Builder
    private record UploadedOrderRow(
        String orderGroupCode,
        String channelType,
        String ordererName,
        String postalCode,
        String city,
        String district,
        String streetAddress,
        String detailAddress,
        String productId,
        String quantity
    ) {
    }
}
