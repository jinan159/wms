package com.teamfresh.wms.domain.order;

import com.teamfresh.wms.domain.BaseEntity;
import com.teamfresh.wms.domain.product.ProductStockHistory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false)
    private ChannelType channelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Embedded
    private Address address;

    @Column(name = "ordered_at", nullable = false)
    private ZonedDateTime orderedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private final List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "order_product_stock_histories",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "product_stock_history_id")
    )
    private final List<ProductStockHistory> productStockHistories = new ArrayList<>();

    public enum ChannelType {
        TEAM_FRESH_MALL
    }

    public enum OrderStatus {
        REGISTERED,
        ACCEPTED,
        PENDING_SHIPMENT,
        PROCESSING_SHIPMENT,
        SHIPMENT_COMPLETED,
        SHIPMENT_CANCELLED,
    }

    @Builder(builderMethodName = "createRegisteredOrder")
    private Order(
        ChannelType channelType,
        String customerName,
        Address address,
        ZonedDateTime orderedAt
    ) {
        this.channelType = channelType;
        this.status = OrderStatus.REGISTERED;
        this.customerName = customerName;
        this.address = address;
        this.orderedAt = orderedAt;
    }

    public void registerOrderItems(List<OrderItem> orderItems) {
        this.orderItems.addAll(orderItems);
        orderItems.forEach(orderItem -> orderItem.registerOrder(this));
    }

    public Map<UUID, Long> getOrderedProductQuantityMap() {
        Map<UUID, Long> productCounts = new HashMap<>();

        for (var orderItem : orderItems) {
            var productId = orderItem.getProductId();
            productCounts.put(
                productId,
                productCounts.getOrDefault(productId, 0L) + orderItem.getQuantity()
            );
        }

        return productCounts;
    }

    public void accept(
        List<ProductStockHistory> productStockHistories
    ) {
        this.status = OrderStatus.ACCEPTED;
        this.productStockHistories.addAll(productStockHistories);
    }
}