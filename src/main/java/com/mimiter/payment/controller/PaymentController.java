package com.mimiter.payment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimiter.payment.annotation.AnonymousAccess;
import com.mimiter.payment.exception.BadRequestException;
import com.mimiter.payment.model.BaseResponse;
import com.mimiter.payment.service.PaymentService;
import com.wechat.pay.java.core.notification.Notification;
import com.wechat.pay.java.core.notification.RequestParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.wechat.pay.java.core.http.Constant.*;

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
                .serialNumber(request.getHeader(WECHAT_PAY_SERIAL))
                .nonce(request.getHeader(WECHAT_PAY_NONCE))
                .signature(request.getHeader(WECHAT_PAY_SIGNATURE))
                .signType(request.getHeader("Wechatpay-Signature-Type"))
                .timestamp(request.getHeader(WECHAT_PAY_TIMESTAMP))
                .body(body)
                .build();
        log.debug("支付/退款回调: requestParam: {}", requestParam);

        ObjectMapper mapper = new ObjectMapper();
        try {
            var tree = mapper.readTree(body);
            String originType = tree.get("resource").get("original_type").textValue();
            if ("transaction".equals(originType)) {
                paymentService.handleTransactionNotification(requestParam);
            } else {
                paymentService.handleRefundNotification(requestParam);
            }
        } catch (JsonProcessingException e) {
            log.error("解析支付通知异常", e);
            throw new BadRequestException("支付通知回调json格式不正确");
        }

        return BaseResponse.ok();
    }
}
