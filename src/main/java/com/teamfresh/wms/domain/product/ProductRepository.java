package com.teamfresh.wms.domain.product;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query("SELECT COUNT(p) FROM Product p WHERE p.id IN :ids")
    long countAllByIds(Collection<UUID> ids);
}
