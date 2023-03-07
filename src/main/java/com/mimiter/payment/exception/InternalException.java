package com.mimiter.payment.exception;

/**
 * 由于程序内部错误导致的异常，属于<b>我们</b>的问题。
 */
public class InternalException extends RuntimeException {

    public InternalException() {
        super();
    }

    public InternalException(String message) {
        super(message);
    }
}
