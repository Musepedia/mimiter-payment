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
@TableName("payment_order")
@EqualsAndHashCode(callSuper = false)
public class Order extends BaseEntity{

    @TableId(value = "out_trade_no", type = IdType.INPUT)
    private String outTradeNo;

    @TableField("transaction_id")
    private String transactionId;

    @TableField("open_id")
    private String openId;

    @TableField("amount")
    private Integer amount;

    @TableField("trade_state")
    private String tradeState;

    @TableField("description")
    private String description;

    @TableField("app_id")
    private String appId;

}
