package com.mimiter.payment.model.entity;

import com.mimiter.payment.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    private String id;
}
