package com.mimiter.payment.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mimiter.payment.model.OutRefund;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OutRefundRepository extends BaseMapper<OutRefund> {
}
