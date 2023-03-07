package com.mimiter.payment.service.impl;

import com.github.wujun234.uid.UidGenerator;
import com.mimiter.payment.PaymentServiceGrpc;
import com.mimiter.payment.PrepayResp;
import com.mimiter.payment.config.WxPaymentConfig;
import com.mimiter.payment.model.request.PrepayReq;
import com.mimiter.payment.service.PaymentService;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@RequiredArgsConstructor
//@Service("paymentService")
@GrpcService
public class PaymentServiceImpl
        extends PaymentServiceGrpc.PaymentServiceImplBase
        implements PaymentService, InitializingBean {

    private final WxPaymentConfig wxPaymentConfig;

    @Resource(name = "defaultUidGenerator")
    private UidGenerator uidGenerator;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    // 15分钟
    private static final long EXPIRE_IN_MILLIS = 15 * 60 * 1000;
    private JsapiServiceExtension jsapiService;

    @Override
    public void afterPropertiesSet() throws Exception {
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPaymentConfig.getMerchantId())
                .privateKeyFromPath(wxPaymentConfig.getPrivateKey())
                .merchantSerialNumber(wxPaymentConfig.getMerchantSerialNumber())
                .apiV3Key(wxPaymentConfig.getApiV3key())
                .build();
        jsapiService = new JsapiServiceExtension.Builder().config(config).build();
    }

    @Override
    @Transactional
    public PrepayWithRequestPaymentResponse prepay(int amount, String description, String openId, long expireInMillis, String appId) {
        Assert.isTrue(amount > 0, "金额必须大于0");
        Assert.hasText(openId, "openId不能为空");
        Assert.hasText(appId, "appid不能为空");
        PrepayRequest request = new PrepayRequest();
        Amount a = new Amount();
        a.setTotal(amount);
        request.setAmount(a);
        request.setAppid(appId);
        request.setMchid(wxPaymentConfig.getMerchantId());
        request.setDescription(description);
        request.setNotifyUrl(wxPaymentConfig.getNotifyUrl());
        request.setOutTradeNo(String.valueOf(uidGenerator.getUID()));
        if (expireInMillis > 0) {
            request.setTimeExpire(DATE_FORMAT.format(new Date(System.currentTimeMillis() + expireInMillis)));
        }
        Payer payer = new Payer();
        payer.setOpenid(openId);
        request.setPayer(payer);

        // TODO create order
        PrepayWithRequestPaymentResponse response = jsapiService.prepayWithRequestPayment(request);

        return response;
    }

    @Override
    public void prepay(com.mimiter.payment.PrepayReq request, StreamObserver<PrepayResp> responseObserver) {
        PrepayWithRequestPaymentResponse prepayResp = prepay(
                request.getAmount(),
                request.getDescription(),
                request.getOpenId(),
                request.getExpireInMillis(),
                request.getAppId());
        PrepayResp resp = PrepayResp.newBuilder()
                .setPackageVal(prepayResp.getPackageVal())
                .setPaySign(prepayResp.getPaySign())
                .setNonceStr(prepayResp.getNonceStr())
                .setTimestamp(prepayResp.getTimeStamp())
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
