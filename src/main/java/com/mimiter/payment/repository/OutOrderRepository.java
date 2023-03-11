package com.mimiter.payment.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mimiter.payment.model.OutOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OutOrderRepository extends BaseMapper<OutOrder> {
}
