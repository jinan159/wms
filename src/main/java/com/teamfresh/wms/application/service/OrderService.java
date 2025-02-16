package com.teamfresh.wms.application.service;

import com.teamfresh.wms.application.dto.OrderItemDto;
import com.teamfresh.wms.application.dto.OrderRegisterRequestDto;
import com.teamfresh.wms.application.exception.ApplicationException;
import com.teamfresh.wms.application.exception.OrderRegisterFailedException;
import com.teamfresh.wms.application.exception.ProductNotFoundException;
import com.teamfresh.wms.application.exception.ProductQuantityNotEnoughException;
import com.teamfresh.wms.domain.order.Order;
import com.teamfresh.wms.domain.order.OrderRepository;
import com.teamfresh.wms.domain.product.ProductRepository;
import com.teamfresh.wms.domain.product.ProductStockHistory.ProductStockHistoryType;
import com.teamfresh.wms.domain.product.ProductStockHistoryRepository;
import com.teamfresh.wms.infra.LockManager;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductStockHistoryRepository productStockHistoryRepository;
    private final LockManager lockManager;

    @Transactional(rollbackFor = Exception.class)
    public UUID register(final OrderRegisterRequestDto request) {
        var productIds = request.orderItems()
            .stream()
            .map(OrderItemDto::productId)
            .collect(Collectors.toSet());

        try {
            // 락 이용하여 주문 처리
            var acceptedOrder = lockManager.useProductsLock(
                productIds,
                () -> {
                    var registeredOrder = orderRepository.save(request.toDomain());

                    return acceptOrder(registeredOrder);
                }
            );

            return acceptedOrder.getId();
        } catch (ApplicationException ae) {
            throw ae;
        } catch (Exception e) {
            throw new OrderRegisterFailedException(e);
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    protected Order acceptOrder(final Order registeredOrder) {
        // 주문 등록
        var orderedProductQuantityMap = registeredOrder.getOrderedProductQuantityMap();
        var products = productRepository.findAllById(orderedProductQuantityMap.keySet());

        if (products.size() != orderedProductQuantityMap.size()) {
            throw new ProductNotFoundException();
        }

        // 상품 재고 차감
        try {
            var productStockHistories = products.stream()
                .map(product -> product.decreaseStock(
                    orderedProductQuantityMap.get(product.getId()),
                    ProductStockHistoryType.PENDING_OUTBOUND
                ))
                .toList();

            // 상품 재고 차감 이력 등록
            productStockHistoryRepository.saveAll(productStockHistories);

            registeredOrder.accept(productStockHistories);
        } catch (IllegalStateException e) {
            throw new ProductQuantityNotEnoughException(e);
        }

        return registeredOrder;
    }
}
