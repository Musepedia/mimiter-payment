package com.mimiter.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mimiter.payment.wx")
public class WxPaymentConfig {

    private String merchantId = "";

    private String privateKey = "id_rsa";
    /** 商户证书序列号 */
    private String merchantSerialNumber = "";
    /** 商户APIV3密钥 */
    private String apiV3key = "";

    private String notifyUrl = "";

}
