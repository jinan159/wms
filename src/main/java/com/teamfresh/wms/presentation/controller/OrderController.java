package com.teamfresh.wms.presentation.controller;

import com.teamfresh.wms.application.dto.OrderCreateRequestDto;
import com.teamfresh.wms.application.dto.OrderUploadRequestDto;
import com.teamfresh.wms.application.service.OrderService;
import com.teamfresh.wms.infra.document.SpreadSheetParser;
import com.teamfresh.wms.presentation.dto.OrderCreateResponseDto;
import com.teamfresh.wms.presentation.dto.OrderUploadResponseDto;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/order/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private static final SpreadSheetOrderCreateRequestMapper spreadSheetOrderCreateRequestMapper = new SpreadSheetOrderCreateRequestMapper();

    private final OrderService orderService;
    private final SpreadSheetParser spreadSheetParser;

    @PostMapping
    public OrderCreateResponseDto create(
        @RequestBody OrderCreateRequestDto requestBody
    ) {
        return new OrderCreateResponseDto(
            orderService.createOrder(requestBody)
        );
    }

    @PostMapping("/upload")
    public OrderUploadResponseDto upload(@RequestPart MultipartFile file) throws IOException {;
        var mappingResult = spreadSheetOrderCreateRequestMapper.map(
            spreadSheetParser.parse(file.getInputStream())
        );

        return new OrderUploadResponseDto(
            orderService.uploadOrders(
                new OrderUploadRequestDto(
                    mappingResult.orderCreateRequests(),
                    mappingResult.productQuantityMap()
                )
            )
        );
    }
}
