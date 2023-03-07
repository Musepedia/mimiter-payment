package com.mimiter.payment.service.impl;

import com.github.wujun234.uid.UidGenerator;
import com.mimiter.payment.config.WxPaymentConfig;
import com.mimiter.payment.model.request.PrepayReq;
import com.mimiter.payment.service.PaymentService;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@RequiredArgsConstructor
@Service("paymentService")
public class PaymentServiceImpl implements PaymentService, InitializingBean {

    private final WxPaymentConfig wxPaymentConfig;

    @Resource(name = "defaultUidGenerator")
    private UidGenerator uidGenerator;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    // 15分钟
    private static final long EXPIRE_IN_MILLIS = 15 * 60 * 1000;
    private JsapiService jsapiService;

    @Override
    public void afterPropertiesSet() throws Exception {
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPaymentConfig.getMerchantId())
                .privateKeyFromPath(wxPaymentConfig.getPrivateKey())
                .merchantSerialNumber(wxPaymentConfig.getMerchantSerialNumber())
                .apiV3Key(wxPaymentConfig.getApiV3key())
                .build();
        jsapiService = new JsapiService.Builder().config(config).build();
    }

    @Override
    public String prepay(PrepayReq req) {
        PrepayRequest request = new PrepayRequest();
        Amount a = new Amount();
        a.setTotal(req.getAmount());
        request.setAmount(a);
        request.setAppid(req.getAppId());
        request.setMchid(wxPaymentConfig.getMerchantId());
        request.setDescription(req.getDescription());
        request.setNotifyUrl(wxPaymentConfig.getNotifyUrl());
        request.setOutTradeNo(String.valueOf(uidGenerator.getUID()));
        if (req.getExpireInMillis() > 0) {
            request.setTimeExpire(DATE_FORMAT.format(new Date(System.currentTimeMillis() + req.getExpireInMillis())));
        }
        Payer payer = new Payer();
        payer.setOpenid(req.getOpenId());
        request.setPayer(payer);
        PrepayResponse response = jsapiService.prepay(request);
        return response.getPrepayId();
    }

}
