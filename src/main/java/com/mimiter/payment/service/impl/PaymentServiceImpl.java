package com.mimiter.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.wujun234.uid.UidGenerator;
import com.google.protobuf.Empty;
import com.mimiter.payment.*;
import com.mimiter.payment.config.WxPaymentConfig;
import com.mimiter.payment.exception.ResourceNotFoundException;
import com.mimiter.payment.model.Order;
import com.mimiter.payment.repository.OrderRepository;
import com.mimiter.payment.service.PaymentService;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.var;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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

    private final OrderRepository orderRepository;

    @Resource(name = "defaultUidGenerator")
    private UidGenerator uidGenerator;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    private JsapiServiceExtension jsapiService;

    private NotificationParser notificationParser;

    private RefundService refundService;

    @Override
    public void afterPropertiesSet() throws Exception {
        var config = new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPaymentConfig.getMerchantId())
                .privateKeyFromPath(wxPaymentConfig.getPrivateKey())
                .merchantSerialNumber(wxPaymentConfig.getMerchantSerialNumber())
                .apiV3Key(wxPaymentConfig.getApiV3key())
                .build();
        jsapiService = new JsapiServiceExtension.Builder().config(config).build();
        notificationParser = new NotificationParser(config);
        refundService = new RefundService.Builder().config(config).build();
    }

    @Override
    @Transactional
    public Pair<PrepayWithRequestPaymentResponse, Order> prepay(
            int amount, String description, String openId, long expireInMillis, String appId) {
        Assert.isTrue(amount > 0, "金额必须大于0");
        Assert.hasText(openId, "openId不能为空");
        Assert.hasText(appId, "appid不能为空");

        // 创建数据库订单
        Order order = createPrepayOrder(openId, amount, description, appId);

        PrepayRequest request = new PrepayRequest();
        Amount a = new Amount();
        a.setTotal(amount);
        request.setAmount(a);
        request.setAppid(appId);
        request.setMchid(wxPaymentConfig.getMerchantId());
        request.setDescription(description);
        request.setNotifyUrl(wxPaymentConfig.getNotifyUrl());
        request.setOutTradeNo(order.getOutTradeNo());
        if (expireInMillis > 0) {
            request.setTimeExpire(DATE_FORMAT.format(new Date(System.currentTimeMillis() + expireInMillis)));
        }
        Payer payer = new Payer();
        payer.setOpenid(openId);
        request.setPayer(payer);

        return Pair.of(jsapiService.prepayWithRequestPayment(request), order);
    }

    private Order createPrepayOrder(String openId, Integer amount, String description, String appId){
        Order order = new Order();
        order.setOpenId(openId);
        order.setAmount(amount);
        order.setTradeState(Transaction.TradeStateEnum.NOTPAY.name());
        order.setDescription(description);
        order.setOutTradeNo(String.valueOf(uidGenerator.getUID()));
        order.setAppId(appId);
        orderRepository.insert(order);

        return order;
    }

    private Order getNotNullByTransactionId(String transactionId){
        Assert.notNull(transactionId, "transactionId不能为空");
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("transaction_id", transactionId);
        Order o = orderRepository.selectOne(wrapper);
        if (o == null) {
            throw new ResourceNotFoundException("找不到transactionId为" + transactionId + "的订单");
        }
        return o;
    }
    private Order getNotNullByOutTradeNo(String outTradeNo){
        Assert.notNull(outTradeNo, "outTradeNo不能为空");
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no", outTradeNo);
        Order o = orderRepository.selectOne(wrapper);
        if (o == null) {
            throw new ResourceNotFoundException("找不到outTradeNo为" + outTradeNo + "的订单");
        }
        return o;
    }


    @Override
    public void handleTransactionNotification(RequestParam requestParam) {
        var transaction = notificationParser.parse(requestParam, Transaction.class);

        Order order = getNotNullByOutTradeNo(transaction.getOutTradeNo());
        if (transaction.getTradeState().equals(Transaction.TradeStateEnum.SUCCESS)){
            Assert.isTrue(order.getTradeState().equals(Transaction.TradeStateEnum.NOTPAY.name()),
                    "订单outTradeNo: " + transaction.getOutTradeNo()
                            + "状态为" + order.getTradeState()
                            + ", 支付失败");
        }

        // 更新订单状态
        order.setTradeState(transaction.getTradeState().name());
        order.setTransactionId(transaction.getTransactionId());
        orderRepository.updateById(order);
    }

    @Override
    public void handleRefundNotification(RequestParam requestParam) {
        var refund = notificationParser.parse(requestParam, Refund.class);

        Order order = getNotNullByOutTradeNo(refund.getOutTradeNo());

        if(refund.getStatus().equals(Status.SUCCESS)){

        }
//         更新订单状态
//        order.setTradeState(Transaction);
//
//        orderRepository.updateById(order);
    }

    @Override
    public void prepay(com.mimiter.payment.PrepayReq request,
                       StreamObserver<PrepayResp> responseObserver) {
        var pair = prepay(
                request.getAmount(),
                request.getDescription(),
                request.getOpenId(),
                request.getExpireInMillis(),
                request.getAppId());
        var prepayResp = pair.getFirst();
        var order = pair.getSecond();
        PrepayResp resp = PrepayResp.newBuilder()
                .setPackageVal(prepayResp.getPackageVal())
                .setPaySign(prepayResp.getPaySign())
                .setNonceStr(prepayResp.getNonceStr())
                .setTimestamp(prepayResp.getTimeStamp())
                .setOutTradeNo(order.getOutTradeNo())
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void getTransaction(GetTransactionReq request, StreamObserver<GetTransactionResp> responseObserver) {
        super.getTransaction(request, responseObserver);
    }

    @Override
    public void closeTransaction(CloseTransactionReq request, StreamObserver<Empty> responseObserver) {
        super.closeTransaction(request, responseObserver);
    }

    @Override
    public void refund(RefundReq request, StreamObserver<RefundResp> responseObserver) {
        Order order = null;
        if (StringUtils.hasText(request.getOutTradeNo())) {
            order = getNotNullByOutTradeNo(request.getOutTradeNo());
        }

        CreateRequest refundReq = new CreateRequest();
        refundReq.setNotifyUrl(wxPaymentConfig.getNotifyUrl());
        refundReq.setReason(request.getReason());

        var refundResp = refundService.create(refundReq);
    }
}
