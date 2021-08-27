package com.github.eric.onlinemallpay.enums;

import com.lly835.bestpay.enums.BestPayTypeEnum;

public enum PayPlatformEnum {
    ALIPAY(1),
    WX(2);

    Integer code;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    PayPlatformEnum(Integer code) {
        this.code = code;
    }

    public static PayPlatformEnum getByPayPlatformEnum(BestPayTypeEnum bestPayTypeEnum) {

        for (PayPlatformEnum payPlatformEnum : PayPlatformEnum.values()) {
            if(payPlatformEnum.name().equals(bestPayTypeEnum.getPlatform().name())){
                return payPlatformEnum;
            }
        }
        throw new RuntimeException("不支持的支付方式："+bestPayTypeEnum.name());
    }
}
