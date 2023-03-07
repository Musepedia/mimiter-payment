package com.mimiter.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 所有实体类的基类，包含了所有实体类的公共属性，所有进数据库的实体类应该继承这个类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity {

    private Date createTime;

    private Date updateTime;

}
