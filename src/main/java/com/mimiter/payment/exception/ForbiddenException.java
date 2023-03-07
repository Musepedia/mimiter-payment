package com.mimiter.payment.exception;

/**
 * 用户已登录，但是权限不足时抛出此异常。
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super();
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
