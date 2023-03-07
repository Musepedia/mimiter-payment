package com.mimiter.payment.controller;

import com.mimiter.payment.model.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @PostMapping
    public BaseResponse<?> prepay() {

        return BaseResponse.ok();
    }
}
