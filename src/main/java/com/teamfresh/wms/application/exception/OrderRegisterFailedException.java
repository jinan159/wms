package com.teamfresh.wms.application.exception;

public class OrderRegisterFailedException extends ApplicationException {
    private static final String message = "주문 등록에 실패하였습니다.";

    public OrderRegisterFailedException(Throwable cause) {
        super(message, cause);
    }
}
