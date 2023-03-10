package com.mimiter.payment.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mimiter.payment.model.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderRepository extends BaseMapper<Order> {
}
