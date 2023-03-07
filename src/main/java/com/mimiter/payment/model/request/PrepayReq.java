package com.mimiter.payment.model.request;

import lombok.Data;

@Data
public class PrepayReq {

    private Integer amount;

    private String description;

    private String openId;

    private long expireInMillis;

    private String appId;
}
