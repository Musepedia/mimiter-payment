package com.mimiter.payment.controller;

import com.mimiter.payment.annotation.AnonymousAccess;
import com.mimiter.payment.model.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @AnonymousAccess
    @PostMapping
    public BaseResponse<?> notifyCallback() {

        return BaseResponse.ok();
    }
}
