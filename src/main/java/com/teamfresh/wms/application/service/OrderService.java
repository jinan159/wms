package com.teamfresh.wms.application.service;

import com.teamfresh.wms.application.dto.OrderCreateRequestDto;
import com.teamfresh.wms.application.dto.OrderItemDto;
import com.teamfresh.wms.application.dto.OrderUploadRequestDto;
import com.teamfresh.wms.application.exception.ApplicationException;
import com.teamfresh.wms.application.exception.OrderRegisterFailedException;
import com.teamfresh.wms.application.exception.ProductNotFoundException;
import com.teamfresh.wms.application.exception.ProductQuantityNotEnoughException;
import com.teamfresh.wms.domain.order.Order;
import com.teamfresh.wms.domain.order.OrderProductStockHistory;
import com.teamfresh.wms.domain.order.OrderProductStockHistoryRepository;
import com.teamfresh.wms.domain.order.OrderRepository;
import com.teamfresh.wms.domain.product.Product;
import com.teamfresh.wms.domain.product.ProductRepository;
import com.teamfresh.wms.domain.product.ProductStockHistory;
import com.teamfresh.wms.domain.product.ProductStockHistory.ProductStockHistoryType;
import com.teamfresh.wms.domain.product.ProductStockHistoryRepository;
import com.teamfresh.wms.infra.lock.LockManager;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
    private final OrderProductStockHistoryRepository orderProductStockHistoryRepository;
    private final LockManager lockManager;


    @Transactional
    public UUID createOrder(@Valid final OrderCreateRequestDto request) {
        var productIds = request.orderItems()
            .stream()
            .map(OrderItemDto::productId)
            .collect(Collectors.toSet());

        try {
            // 여러 상품에 대해 락 획득 & 주문 단건 처리
            var order = request.toDomain();
            var acceptedOrder = lockManager.useProductsLock(
                productIds,
                () -> acceptOrder(orderRepository.save(order))
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
            var productStockHistories = decreaseAllProductStockWithHistory(
                products,
                orderedProductQuantityMap
            );

            saveAllOrderProductStockHistories(
                registeredOrder.getId(),
                productStockHistories
            );
        } catch (IllegalStateException e) {
            throw new ProductQuantityNotEnoughException(e);
        }

        return registeredOrder;
    }

    @Transactional
    public List<UUID> uploadOrders(@Valid final OrderUploadRequestDto request) {
        var productQuantityMap = request.productQuantityMap();
        var productIds = productQuantityMap.keySet();

        // 여러 상품에 대해 락 획득 & 주문 복수 처리
        var acceptedOrders = lockManager.useProductsLock(
            productIds,
            () -> acceptOrders(request, productQuantityMap)
        );

        return acceptedOrders.stream()
            .map(Order::getId)
            .toList();
    }

    @Transactional(propagation = Propagation.NESTED)
    protected List<Order> acceptOrders(
        final OrderUploadRequestDto mappingResult,
        final Map<UUID, Long> productQuantityMap
    ) {
        var products = productRepository.findAllById(productQuantityMap.keySet());

        if (products.size() != productQuantityMap.size()) {
            throw new ProductNotFoundException();
        }

        var orderGroupCode = UUID.randomUUID();
        var orders = mappingResult.orderCreateRequests()
            .stream()
            .map(orderCreateRequest -> {
                var order = orderCreateRequest.toDomain();
                order.assignOrderGroupCode(orderGroupCode);
                order.accept();
                return order;
            })
            .toList();

        var productStockHistories = decreaseAllProductStockWithHistory(
            products,
            productQuantityMap
        );

        saveAllGroupedOrderProductStockHistories(
            orderGroupCode,
            productStockHistories
        );

        orderRepository.saveAll(orders);

        return orders;
    }

    private List<ProductStockHistory> decreaseAllProductStockWithHistory(
        List<Product> products,
        Map<UUID, Long> productQuantityMap
    ) {
        var productStockHistories = products.stream()
            .map(product -> product.decreaseStockWithHistory(
                productQuantityMap.get(product.getId()),
                ProductStockHistoryType.PENDING_OUTBOUND
            ))
            .toList();

        // 상품 재고 차감 이력 등록
        return productStockHistoryRepository.saveAll(productStockHistories);
    }

    private void saveAllOrderProductStockHistories(
        UUID orderId,
        List<ProductStockHistory> productStockHistories
    ) {
        orderProductStockHistoryRepository.saveAll(
            productStockHistories.stream()
                .map(productStockHistory -> OrderProductStockHistory.ofSingleOrder(
                    orderId,
                    productStockHistory.getId()
                ))
                .toList()
        );
    }

    private void saveAllGroupedOrderProductStockHistories(
        UUID orderGroupCode,
        List<ProductStockHistory> productStockHistories
    ) {
        orderProductStockHistoryRepository.saveAll(
            productStockHistories.stream()
                .map(productStockHistory -> OrderProductStockHistory.ofGroupOrder(
                    orderGroupCode,
                    productStockHistory.getId()
                ))
                .toList()
        );
    }
}
