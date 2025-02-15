package com.teamfresh.wms.infra;

import com.teamfresh.wms.infra.exception.LockAcquireFailedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RedisLockManager implements LockManager {
    private static final String PRODUCTS_LOCK_GROUP_KEY = "products:lock";

    private final RedissonClient redisClient;

    @Override
    public <T> T useProductsLock(
        Collection<UUID> productIds,
        Supplier<T> innerCodeBLock
    ) {
        return useProductsLock(
            productIds,
            new LockDuration(1, 1, TimeUnit.SECONDS),
            innerCodeBLock);
    }

    @Override
    public <T> T useProductsLock(Collection<UUID> productIds, LockDuration duration, Supplier<T> innerCodeBLock) {
        var multiLock = getSortedMultiLock(productIds);

        try {
            if (multiLock.tryLock(
                duration.waitTime(),
                duration.leaseTime(),
                duration.timeUnit()
            )) {
                return innerCodeBLock.get();
            }

            throw new LockAcquireFailedException();
        } catch (InterruptedException e) {
            throw new LockAcquireFailedException(e);
        } finally {
            multiLock.unlock();
        }
    }

    private RLock getSortedMultiLock(Collection<UUID> productIds) {
        return redisClient.getMultiLock(
            PRODUCTS_LOCK_GROUP_KEY,
            new ArrayList<>(productIds)
        );
    }
}