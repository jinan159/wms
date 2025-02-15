package com.teamfresh.wms.application.exception;

public class ProductNotFoundException extends ApplicationException {
    public ProductNotFoundException() {
        super("상품을 찾을 수 없습니다.");
    }
}
