package com.teamfresh.wms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long stockQuantity;

    public boolean isStockEnough(Long requiredQuantity) {
        return stockQuantity >= requiredQuantity;
    }

    public ProductStockHistory orderedBy(Order order) {
        var orderedQuantity = order.getOrderItems()
            .stream()
            .filter(item -> item.getProductId().equals(id))
            .mapToLong(OrderItem::getQuantity)
            .sum();

        stockQuantity -= orderedQuantity;

        return ProductStockHistory.createPendingOutboundHistory()
            .productId(id)
            .quantity(orderedQuantity)
            .order(order)
            .build();
    }
}