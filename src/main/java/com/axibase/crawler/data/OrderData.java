package com.axibase.crawler.data;

import com.axibase.crawler.orders.OrderInfo;

import java.util.Date;
import java.util.List;

public class OrderData {
    public final int orderId;

    public final Date date;

    public final List<OrderInfo> products;

    public OrderData(int orderId, Date date, List<OrderInfo> products) {
        this.orderId = orderId;
        this.date = date;
        this.products = products;
    }
}
