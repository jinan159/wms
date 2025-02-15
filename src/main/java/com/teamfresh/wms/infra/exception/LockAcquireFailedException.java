package com.teamfresh.wms.infra.exception;

public class LockAcquireFailedException extends RuntimeException {
    private static final String message = "락 획득에 실패했습니다.";

    public LockAcquireFailedException() {
        super(message);
    }

    public LockAcquireFailedException(Throwable cause) {
        super(message, cause);
    }
}
