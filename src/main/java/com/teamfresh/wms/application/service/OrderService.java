package com.teamfresh.wms.application.service;

import com.teamfresh.wms.application.dto.OrderRegisterRequestDto;
import com.teamfresh.wms.application.exception.ApplicationException;
import com.teamfresh.wms.application.exception.OrderRegisterFailedException;
import com.teamfresh.wms.application.exception.ProductNotFoundException;
import com.teamfresh.wms.application.exception.ProductQuantityNotEnoughException;
import com.teamfresh.wms.domain.Order;
import com.teamfresh.wms.domain.OrderRepository;
import com.teamfresh.wms.domain.ProductRepository;
import com.teamfresh.wms.domain.ProductStockHistoryRepository;
import com.teamfresh.wms.infra.LockManager;
import java.util.Map;
import java.util.UUID;
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
    public UUID registerOrder(final OrderRegisterRequestDto request) {
        var order = request.toDomain();
        var orderedProductQuantityMap = order.getOrderedProductQuantityMap();

        // 상품 ID 추출
        var orderedProductIds = orderedProductQuantityMap.keySet();

        try {
            // 락 이용하여 주문 등록 처리
            return lockManager.useProductsLock(
                orderedProductIds,
                () -> performRegisterOrder(
                    order,
                    orderedProductQuantityMap
                )
            );
        } catch (ApplicationException ae) {
            throw ae;
        } catch (Exception e) {
            throw new OrderRegisterFailedException(e);
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    protected UUID performRegisterOrder(
        final Order order,
        final Map<UUID, Long> orderedProductQuantityMap
    ) {
        var productIds = orderedProductQuantityMap.keySet();
        var products = productRepository.findAllById(productIds);

        if (products.size() != productIds.size()) {
            throw new ProductNotFoundException();
        }

        var isAllProductQuantityEnough = products.stream()
            .allMatch(product -> product.isStockEnough(orderedProductQuantityMap.get(product.getId())));

        if (!isAllProductQuantityEnough) {
            throw new ProductQuantityNotEnoughException();
        }

        // 주문 등록
        var savedOrder = orderRepository.save(order);

        // 상품 재고 차감
        var productStockHistories = products.stream()
            .map(product -> product.orderedBy(savedOrder))
            .toList();

        // 주문 수락 처리
        savedOrder.accepted();

        // 상품 재고 차감 이력 등록
        productStockHistoryRepository.saveAll(productStockHistories);

        return savedOrder.getId();
    }
}
