package com.mimiter.payment.service;

import com.mimiter.payment.model.request.PrepayReq;

/**
 * 支付服务
 */
public interface PaymentService {

    String prepay(PrepayReq req);
}
