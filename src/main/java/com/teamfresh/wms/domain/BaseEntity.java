package com.teamfresh.wms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(BaseEntity.AuditListener.class)
public abstract class BaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public static class AuditListener {

        @PrePersist
        public void prePersist(BaseEntity entity) {
            ZonedDateTime now = ZonedDateTime.now();
            entity.createdAt = now;
            entity.updatedAt = now;
        }

        @PreUpdate
        public void preUpdate(BaseEntity entity) {
            entity.updatedAt = ZonedDateTime.now();
        }
    }
}
