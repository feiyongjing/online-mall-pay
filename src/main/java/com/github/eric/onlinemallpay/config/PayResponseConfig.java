package com.github.eric.onlinemallpay.config;

import com.lly835.bestpay.config.AliPayConfig;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayResponseConfig {

    @Bean
    public BestPayService newPayResponse(){
        //微信配置
        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setAppId("xxxxx");          //公众号Id
//        wxPayConfig.setMiniAppId("xxxxx");      //小程序Id
//        wxPayConfig.setAppAppId("xxxxx");       //移动AppId
//支付商户资料
        wxPayConfig.setMchId("xxxxxx");
        wxPayConfig.setMchKey("xxxxxxx");
        wxPayConfig.setNotifyUrl("http://xxxxx"+"/pay/notify");

        // 支付宝配置
        AliPayConfig aliPayConfig = new AliPayConfig();
        aliPayConfig.setAppId("xxxxxx");
        aliPayConfig.setPrivateKey("xxxxxx");
        aliPayConfig.setAliPayPublicKey("xxxxxx");
        aliPayConfig.setReturnUrl("http://xxxxx"); // 同步跳转页面，不保证支付成功
        aliPayConfig.setNotifyUrl("http://xxxxx"+"/pay/notify"); // 异步支付结果通知

        BestPayServiceImpl bestPayService = new BestPayServiceImpl();
        bestPayService.setWxPayConfig(wxPayConfig);
        bestPayService.setAliPayConfig(aliPayConfig);

        return bestPayService;
    }

}
