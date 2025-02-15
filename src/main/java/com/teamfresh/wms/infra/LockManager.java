package com.teamfresh.wms.infra;

import java.util.Collection;
import java.util.UUID;

public interface LockManager {
    void productsLock(Collection<UUID> productIds);

    void productsUnlock(Collection<UUID> productIds);
}
