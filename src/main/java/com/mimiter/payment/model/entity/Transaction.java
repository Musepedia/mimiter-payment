package com.mimiter.payment.model.entity;

import com.mimiter.payment.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {

    private String outTradeNo;

    private String transactionId;

    private Integer amount;


}
