package com.teamfresh.wms.infra.lock;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface LockManager {
    <T> T useProductsLock(Collection<UUID> productIds, Supplier<T> innerCodeBLock);

    <T> T useProductsLock(Collection<UUID> productIds, LockDuration duration, Supplier<T> innerCodeBLock);

    record LockDuration(
        long waitTime,
        long leaseTime,
        TimeUnit timeUnit
    ) {
    }
}
