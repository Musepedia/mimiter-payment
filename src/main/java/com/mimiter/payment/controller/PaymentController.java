package com.mimiter.payment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimiter.payment.annotation.AnonymousAccess;
import com.mimiter.payment.model.BaseResponse;
import com.mimiter.payment.service.PaymentService;
import com.wechat.pay.java.core.notification.Notification;
import com.wechat.pay.java.core.notification.RequestParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @AnonymousAccess
    @PostMapping("/notify")
    public BaseResponse<?> notifyCallback(@RequestBody String body, HttpServletRequest request) {
        RequestParam requestParam = new RequestParam
                .Builder()
                .serialNumber(request.getHeader("Wechatpay-Serial"))
                .nonce(request.getHeader("Wechatpay-Nonce"))
                .signature(request.getHeader("Wechatpay-Signature"))
                .signType(request.getHeader("Wechatpay-Signature-Type"))
                .body(body)
                .build();
        log.debug("支付/退款回调: requestParam: {}", requestParam);

        ObjectMapper mapper = new ObjectMapper();
        try {
            Notification notification = mapper.readValue(body, Notification.class);
            if ("transaction".equals(notification.getResource().getOriginalType())) {
                paymentService.handleTransactionNotification(requestParam);
            } else {
                paymentService.handleRefundNotification(requestParam);
            }
        } catch (JsonProcessingException e) {
            log.error("解析支付通知异常", e);
            return BaseResponse.error(e.getMessage());
        }

        return BaseResponse.ok();
    }
}
