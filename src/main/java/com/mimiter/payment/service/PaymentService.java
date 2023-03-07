package com.mimiter.payment.service;

import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;

/**
 * 支付服务
 */
public interface PaymentService {

    PrepayWithRequestPaymentResponse prepay(int amount, String description,
                                            String openId, long expireInMillis, String appId);
}
