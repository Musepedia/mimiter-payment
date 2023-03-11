package com.mimiter.payment.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("payment_refund")
@EqualsAndHashCode(callSuper = false)
public class OutRefund extends BaseEntity{

    @TableId(value = "out_refund_no", type = IdType.INPUT)
    private String outRefundNo;

    @TableField("out_trade_no")
    private String outTradeNo;

    @TableField("refund_id")
    private String refundId;

    @TableField("transaction_id")
    private String transactionId;

    @TableField("payer_refund")
    private Integer payerRefund;
}
