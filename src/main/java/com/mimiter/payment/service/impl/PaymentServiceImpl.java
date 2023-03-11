package com.mimiter.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.wujun234.uid.UidGenerator;
import com.google.protobuf.Empty;
import com.mimiter.payment.*;
import com.mimiter.payment.config.WxPaymentConfig;
import com.mimiter.payment.exception.ResourceNotFoundException;
import com.mimiter.payment.model.OutOrder;
import com.mimiter.payment.model.OutRefund;
import com.mimiter.payment.repository.OutOrderRepository;
import com.mimiter.payment.repository.OutRefundRepository;
import com.mimiter.payment.service.PaymentService;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
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

    private final OutOrderRepository orderRepository;

    private final OutRefundRepository refundRepository;

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
    public Pair<PrepayWithRequestPaymentResponse, OutOrder> prepay(
            int amount, String description, String openId, long expireInMillis, String appId) {
        Assert.isTrue(amount > 0, "金额必须大于0");
        Assert.hasText(openId, "openId不能为空");
        Assert.hasText(appId, "appid不能为空");

        // 创建数据库订单
        OutOrder order = createPrepayOrder(openId, amount, description, appId);

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

    private OutOrder createPrepayOrder(String openId, Integer amount, String description, String appId) {
        OutOrder order = new OutOrder();
        order.setOpenId(openId);
        order.setAmount(amount);
        order.setTradeState(Transaction.TradeStateEnum.NOTPAY.name());
        order.setDescription(description);
        order.setOutTradeNo(String.valueOf(uidGenerator.getUID()));
        order.setAppId(appId);
        orderRepository.insert(order);

        return order;
    }

    private OutOrder getNotNullByTransactionId(String transactionId) {
        Assert.notNull(transactionId, "transactionId不能为空");
        QueryWrapper<OutOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("transaction_id", transactionId);
        OutOrder o = orderRepository.selectOne(wrapper);
        if (o == null) {
            throw new ResourceNotFoundException("找不到transactionId为" + transactionId + "的订单");
        }
        return o;
    }

    private OutOrder getNotNullByOutTradeNo(String outTradeNo) {
        Assert.notNull(outTradeNo, "outTradeNo不能为空");
        QueryWrapper<OutOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no", outTradeNo);
        OutOrder o = orderRepository.selectOne(wrapper);
        if (o == null) {
            throw new ResourceNotFoundException("找不到outTradeNo为" + outTradeNo + "的订单");
        }
        return o;
    }


    @Override
    public void handleTransactionNotification(RequestParam requestParam) {
        var transaction = notificationParser.parse(requestParam, Transaction.class);

        OutOrder order = getNotNullByOutTradeNo(transaction.getOutTradeNo());
        if (transaction.getTradeState().equals(Transaction.TradeStateEnum.SUCCESS)) {
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
    @Transactional
    public void handleRefundNotification(RequestParam requestParam) {
        var refund = notificationParser.parse(requestParam, Refund.class);

        OutOrder order = getNotNullByOutTradeNo(refund.getOutTradeNo());

        // 更新订单状态
        // REFUND只代表有退款，无法确定是否已经全额退款
        order.setTradeState(Transaction.TradeStateEnum.REFUND.name());
        orderRepository.updateById(order);

        // 保存Refund记录
//        OutRefund outRefund = new OutRefund();
//        outRefund.setOutRefundNo(refund.getOutRefundNo());
//        outRefund.setOutTradeNo(refund.getOutTradeNo());
//        outRefund.setRefundId(refund.getRefundId());
//        outRefund.setTransactionId(refund.getTransactionId());
//        if (refund.getAmount() != null && refund.getAmount().getPayerRefund() != null){
//            outRefund.setPayerRefund((int)(long)refund.getAmount().getRefund());
//        }
//        refundRepository.insert(outRefund);
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
        Transaction transaction = null;
        if (StringUtils.hasText(request.getOutTradeNo())) {
            var query = new QueryOrderByOutTradeNoRequest();
            query.setOutTradeNo(request.getOutTradeNo());
            query.setMchid(wxPaymentConfig.getMerchantId());
            transaction = jsapiService.queryOrderByOutTradeNo(query);
        } else if (StringUtils.hasText(request.getTransactionId())) {
            var query = new QueryOrderByIdRequest();
            query.setTransactionId(request.getTransactionId());
            query.setMchid(wxPaymentConfig.getMerchantId());
            transaction = jsapiService.queryOrderById(query);
        } else {
            responseObserver.onNext(null);
            responseObserver.onCompleted();
            return;
        }

        var resp = GetTransactionResp.newBuilder()
                .setAppId(transaction.getAppid())
                .setOpenId(transaction.getPayer().getOpenid())
                .setTransactionId(transaction.getTransactionId())
                .setMchId(transaction.getMchid())
                .setOutTradeNo(transaction.getOutTradeNo())
                .setPayerTotal(transaction.getAmount().getPayerTotal())
                .setTotal(transaction.getAmount().getTotal())
                .setTradeState(transaction.getTradeState().name())
                .setTradeType(transaction.getTradeType().name())
                .setSuccessTime(transaction.getSuccessTime())
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void closeTransaction(CloseTransactionReq request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(new UnsupportedOperationException("暂不支持RPC调用关闭订单"));
        responseObserver.onCompleted();
    }

    @Override
    public void refund(RefundReq request, StreamObserver<RefundResp> responseObserver) {
        CreateRequest refundReq = new CreateRequest();
        OutOrder order = null;
        if (StringUtils.hasText(request.getOutTradeNo())) {
            order = getNotNullByOutTradeNo(request.getOutTradeNo());
            refundReq.setOutTradeNo(request.getOutTradeNo());
        } else if (StringUtils.hasText(request.getTransactionId())) {
            order = getNotNullByTransactionId(request.getTransactionId());
            refundReq.setTransactionId(request.getTransactionId());
        } else {
            throw new IllegalArgumentException("商户订单号和交易ID不能为空");
        }

        refundReq.setNotifyUrl(wxPaymentConfig.getNotifyUrl());
        refundReq.setReason(request.getReason());
        refundReq.setOutRefundNo(String.valueOf(uidGenerator.getUID()));
        AmountReq amount = new AmountReq();
        amount.setRefund((long) request.getRefund());
        amount.setTotal((long) (int) order.getAmount());
        amount.setCurrency("CNY");
        refundReq.setAmount(amount);

        try {
            var refundResp = refundService.create(refundReq);
            responseObserver.onNext(RefundResp.newBuilder().build());
        } catch (ServiceException e) {
            e.printStackTrace();
            responseObserver.onError(e);
        }

        responseObserver.onCompleted();
    }
}
