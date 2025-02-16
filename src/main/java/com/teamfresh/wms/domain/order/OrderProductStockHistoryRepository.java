package com.teamfresh.wms.domain.order;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductStockHistoryRepository extends JpaRepository<OrderProductStockHistory, UUID> {
}
