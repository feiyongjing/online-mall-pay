package com.github.eric.onlinemallpay.controller;

import com.alibaba.fastjson.JSON;
import com.github.eric.onlinemallpay.dao.MyPayInfoMapper;
import com.github.eric.onlinemallpay.enums.PayPlatformEnum;
import com.github.eric.onlinemallpay.generate.entity.PayInfo;
import com.github.eric.onlinemallpay.generate.mapper.PayInfoMapper;
import com.github.eric.onlinemallpay.service.impl.PaySeriveImpl;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayResponse;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;

@Controller
@RequestMapping("/pay")
public class PayController {

    private static final String QUEUE_PAY_NOTIFY="payNotify";
    @Autowired
    private PaySeriveImpl paySeriveImpl;

    @Autowired
    PayInfoMapper payInfoMapper;

    @Autowired
    MyPayInfoMapper myPayInfoMapper;

    @Autowired
    WxPayConfig wxPayConfig;

    @Autowired
    AmqpTemplate amqpTemplate;

    @GetMapping("/create")
    public ModelAndView create(@RequestParam("orderId") String orderId,
                               @RequestParam("amount") BigDecimal amount,
                               @RequestParam("payType") BestPayTypeEnum bestPayTypeEnum) {
        // 下单，订单信息写入数据库
        PayInfo payInfo = new PayInfo(Long.valueOf(orderId)
                , PayPlatformEnum.getByPayPlatformEnum(bestPayTypeEnum).getCode(),
                "",
                OrderStatusEnum.NOTPAY.name(),
                amount);
        payInfoMapper.insertSelective(payInfo);

        // 解决重复支付就要调用支付平台的API关闭上一次的支付

        //请求支付
        PayResponse payResponse = paySeriveImpl.create(orderId, amount, bestPayTypeEnum);

        HashMap<String, String> map = new HashMap<>();
        if (bestPayTypeEnum.equals(BestPayTypeEnum.WXPAY_NATIVE)) {
            map.put("codeUrl", payResponse.getCodeUrl());
            map.put("orderId",orderId);
            map.put("returnUrl",wxPayConfig.getReturnUrl());

            return new ModelAndView("createForWxNative", map);
        } else if (bestPayTypeEnum.equals(BestPayTypeEnum.ALIPAY_PC)) {
            map.put("body", payResponse.getBody());
            return new ModelAndView("createForAlipayPc", map);
        }

        throw new RuntimeException("支付方式不支持，只支持WXPAY_NATIVE和ALIPAY_PC");

    }

    @PostMapping("/notify")
    @ResponseBody
    public Object asyncNotify(@RequestBody String notifyData) {
        // 验签
        PayResponse payResponse = paySeriveImpl.asyncNotify(notifyData);

        // 金额校验(从数据库查金额)
        PayInfo payInfo = myPayInfoMapper.selectByOrderNo(Long.valueOf(payResponse.getOrderId()));
        if(payInfo==null){
            // 抛出异常之前发短信或其他方式通知开发人员，出现了不正常的情况
            throw new RuntimeException("数据库中的支付信息表通过order_no="+payResponse.getOrderId()+"查询的支付金额是null");
        }

        // 判断是否未支付
        if(!Objects.equals(payInfo.getPlatformStatus(),OrderStatusEnum.SUCCESS.name())){
            // 未支付
            // 判断异步通知的金额与数据库的金额进行比较
            if(payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount()))!=0){
                // 抛出异常之前应该发短信或其他方式通知开发人员，出现了不正常的情况
                throw new RuntimeException("异步通知的金额与数据库中的不一致，"+"order_no="+payResponse.getOrderId());
            }
            // 修改订单的支付状态
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());
            payInfo.setUpdateTime(null);
            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }

        // TODO online-mall-pay需要发送MQ消息，online-mall接收MQ消息
        amqpTemplate.convertAndSend(QUEUE_PAY_NOTIFY, JSON.toJSON(payInfo));

        // 返回消息给微信或支付宝，不要在回调了
        if (payResponse.getPayPlatformEnum().equals(BestPayPlatformEnum.WX)) {
            return new WxOrderQueryResponse("SUCCESS", "成功");
        } else if (payResponse.getPayPlatformEnum().equals(BestPayPlatformEnum.ALIPAY)) {
            return "success";
        }
        throw new RuntimeException("支付异步通知中错误的支付平台");
    }

    @GetMapping("/queryByOrderId")
    @ResponseBody
    public Object asyncNotify(@RequestParam("orderId") Long orderId) {
        return myPayInfoMapper.selectByOrderNo(orderId);
    }

    static class WxOrderQueryResponse {
        String code;
        String massage;

        public WxOrderQueryResponse(String code, String massage) {
            this.code = code;
            this.massage = massage;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMassage() {
            return massage;
        }

        public void setMassage(String massage) {
            this.massage = massage;
        }
    }
}
