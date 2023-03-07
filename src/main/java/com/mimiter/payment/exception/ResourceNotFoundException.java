package com.mimiter.payment.exception;

/**
 * 查找资源不存在时抛出，可以是根据id查找数据不存在或者获取静态资源不存在等。
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
