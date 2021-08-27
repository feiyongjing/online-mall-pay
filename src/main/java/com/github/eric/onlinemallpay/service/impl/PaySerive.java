package com.github.eric.onlinemallpay.service.impl;

import com.github.eric.onlinemallpay.service.BaseService;
import com.lly835.bestpay.config.AliPayConfig;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaySerive implements BaseService {
    @Autowired
    BestPayService bestPayService;

    @Override
    public PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum) {
        if( !bestPayTypeEnum.equals(BestPayTypeEnum.WXPAY_NATIVE) &&
                !bestPayTypeEnum.equals(BestPayTypeEnum.ALIPAY_PC)){
            throw new RuntimeException("支付方式不支持，只支持WXPAY_NATIVE和ALIPAY_PC");
        }

        PayRequest payRequest = new PayRequest();
        payRequest.setPayTypeEnum(bestPayTypeEnum);
        payRequest.setOrderId(orderId);
        payRequest.setOrderName("微信公众账号支付订单");
        payRequest.setOrderAmount(amount.doubleValue());



//        payRequest.setOpenid("openid_xxxxxx");
        PayResponse payResponse = bestPayService.pay(payRequest);
        return payResponse;
    }

    public PayResponse asyncNotify(String notifyData) {
        return bestPayService.asyncNotify(notifyData);
    }
}
