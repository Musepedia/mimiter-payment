package com.mimiter.payment.annotation;

import java.lang.annotation.*;

/**
 * 用于controller的方法上，表示一个接口不需要登录即可访问。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnonymousAccess {
}
