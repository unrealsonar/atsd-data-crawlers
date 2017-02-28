package com.axibase.crawler.orders;

import java.util.Date;

public class OrderInfo {

    public final Date date;

    public final int productId;

    public final String productName;

    public final Double amount;

    public final Double originalPrice;

    public final String units;

    public OrderInfo(
            Date date,
            int productId,
            String productName,
            Double amount,
            Double originalPrice,
            String units) {
        this.date = date;
        this.productId = productId;
        this.productName = productName;
        this.amount = amount;
        this.originalPrice = originalPrice;
        this.units = units;
    }
}
