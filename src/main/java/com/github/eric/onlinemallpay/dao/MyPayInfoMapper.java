package com.github.eric.onlinemallpay.dao;

import com.github.eric.onlinemallpay.generate.entity.PayInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyPayInfoMapper {

    PayInfo selectByOrderNo(Long orderNo);
}
