package com.teamfresh.wms.domain.order;

import com.teamfresh.wms.domain.BaseEntity;
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
@Table(name = "order_product_stock_histories")
public class OrderProductStockHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "order_group_code")
    private UUID orderGroupCode;

    @Column(name = "product_stock_history_id", nullable = false)
    private UUID productStockHistoryId;

    private OrderProductStockHistory(
        UUID orderId,
        UUID orderGroupCode,
        UUID productStockHistoryId
    ) {
        this.orderId = orderId;
        this.orderGroupCode = orderGroupCode;
        this.productStockHistoryId = productStockHistoryId;
    }

    public static OrderProductStockHistory ofSingleOrder(
        UUID orderId,
        UUID productStockHistoryId
    ) {
        return new OrderProductStockHistory(
            orderId,
            null,
            productStockHistoryId
        );
    }

    public static OrderProductStockHistory ofGroupOrder(
        UUID orderGroupCode,
        UUID productStockHistoryId
    ) {
        return new OrderProductStockHistory(
            null,
            orderGroupCode,
            productStockHistoryId
        );
    }
}
