package com.teamfresh.wms.presentation.controller;

import com.teamfresh.wms.application.dto.AddressDto;
import com.teamfresh.wms.application.dto.OrderCreateRequestDto;
import com.teamfresh.wms.application.dto.OrderItemDto;
import com.teamfresh.wms.application.dto.OrdererDto;
import com.teamfresh.wms.domain.order.Order;
import com.teamfresh.wms.infra.document.SpreadSheetDocument;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Builder;

public class SpreadSheetOrderCreateRequestMapper {
    public MappingResult map(SpreadSheetDocument document) {
        var productQuantityMap = new HashMap<UUID, Long>();
        var uploadedOrders = mapToUploadedOrders(document);

        uploadedOrders.forEach(row ->
            productQuantityMap.merge(
                UUID.fromString(row.productId),
                Long.parseLong(row.quantity),
                Long::sum
            )
        );

        var groupedRows = uploadedOrders.stream()
            .collect(
                Collectors.groupingBy(
                    row -> row.orderGroupCode,
                    Collectors.mapping(
                        row -> row,
                        Collectors.toList()
                    )
                )
            );

        var orderCreateRequests = groupedRows.values().stream()
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

        return new MappingResult(
            orderCreateRequests,
            productQuantityMap
        );
    }

    private List<UploadedOrderRow> mapToUploadedOrders(SpreadSheetDocument document) {
        var firstSheet = document.get(0);

        return firstSheet.rows()
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

    public record MappingResult(
        List<OrderCreateRequestDto> orderCreateRequests,
        Map<UUID, Long> productQuantityMap
    ) {
    }
}
