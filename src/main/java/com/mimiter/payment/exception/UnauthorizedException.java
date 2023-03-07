package com.mimiter.payment.exception;

/**
 * 用户<b>未登录</b>但是访问了需要登录才能访问的资源时抛出。
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
