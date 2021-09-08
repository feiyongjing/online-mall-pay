package com.github.eric.onlinemallpay.service;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

public interface PayService {
   PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);
}
