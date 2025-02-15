package com.teamfresh.wms.application.exception;

public class ProductQuantityNotEnoughException extends ApplicationException {
    public ProductQuantityNotEnoughException() {
        super("상품의 재고가 부족합니다.");
    }
}
