package com.teamfresh.wms.domain.product;

import com.teamfresh.wms.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
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

    public void decreaseStock(long quantity) {
        if (!isStockEnough(quantity)) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        stockQuantity -= quantity;
    }


    public ProductStockHistory decreaseStockWithHistory(
        long quantity,
        ProductStockHistory.ProductStockHistoryType type
    ) {
        decreaseStock(quantity);

        if (Objects.requireNonNull(type) == ProductStockHistory.ProductStockHistoryType.PENDING_OUTBOUND) {
            return ProductStockHistory.createPendingOutboundHistory()
                .productId(id)
                .quantity(quantity)
                .build();
        }

        throw new UnsupportedOperationException();
    }
}