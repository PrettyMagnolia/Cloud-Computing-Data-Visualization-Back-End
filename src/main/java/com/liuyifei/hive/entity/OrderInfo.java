package com.liuyifei.hive.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author 2051196 刘一飞
 * @Date 2022/12/16
 * @JDKVersion 17.0.4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo {
    String product_id;
    Integer sale_num;
    String sum_pay;
    Integer rank_sale_num;
}
