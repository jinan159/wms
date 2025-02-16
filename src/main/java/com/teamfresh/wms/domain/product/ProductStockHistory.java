package com.teamfresh.wms.domain.product;

import com.teamfresh.wms.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "product_stock_histories")
public class ProductStockHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ProductStockHistoryType type;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    public enum ProductStockHistoryType {
        INBOUND,
        PENDING_OUTBOUND,
        OUTBOUND,
        OUTBOUND_CANCELLED
    }

    @Builder(builderMethodName = "createPendingOutboundHistory")
    private ProductStockHistory(
        UUID productId,
        long quantity
    ) {
        this.productId = productId;
        this.type = ProductStockHistoryType.PENDING_OUTBOUND;
        this.quantity = quantity;
    }
}