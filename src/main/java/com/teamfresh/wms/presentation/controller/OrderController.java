package com.teamfresh.wms.presentation.controller;

import com.teamfresh.wms.application.dto.OrderRegisterRequestDto;
import com.teamfresh.wms.application.service.OrderService;
import com.teamfresh.wms.presentation.dto.OrderRegisteredResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public OrderRegisteredResponseDto register(
        @RequestBody @Valid OrderRegisterRequestDto requestBody
    ) {
        return new OrderRegisteredResponseDto(
            orderService.register(requestBody)
        );
    }
}
