package com.teamfresh.wms.infra;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RedisLockManager implements LockManager {
    @Override
    public void productsLock(Collection<UUID> productIds) {
        /*
        TODO 구현 필요
        getSortedProductIds(productIds).forEach(productId -> {
            redis.lock(productId);
        });
        */
    }

    @Override
    public void productsUnlock(Collection<UUID> productIds) {
        /*
        TODO 구현 필요
        getSortedProductIds(productIds).forEach(productId -> {
            redis.unlock(productId);
        });
        */
    }

    private List<UUID> getSortedProductIds(Collection<UUID> productIds) {
        return productIds.stream().sorted().toList();
    }
}
