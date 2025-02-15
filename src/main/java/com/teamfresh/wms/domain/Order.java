package com.teamfresh.wms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "orders")
public class Order {
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

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @OneToMany
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems;

    public enum ChannelType {
        TEAM_FRESH_MALL
    }

    public enum OrderStatus {
        REQUESTED,
        ORDERED,
        PENDING_SHIPMENT,
        PROCESSING_SHIPMENT,
        SHIPMENT_COMPLETED,
        SHIPMENT_CANCELLED,
    }
}