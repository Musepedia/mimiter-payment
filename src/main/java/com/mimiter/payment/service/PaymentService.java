package com.mimiter.payment.service;

import com.mimiter.payment.model.OutOrder;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import org.springframework.data.util.Pair;

/**
 * 支付服务
 */
public interface PaymentService {

    Pair<PrepayWithRequestPaymentResponse, OutOrder> prepay(int amount, String description,
                                                            String openId, long expireInMillis, String appId);

    void handleTransactionNotification(RequestParam requestParam);

    void handleRefundNotification(RequestParam requestParam);
}
