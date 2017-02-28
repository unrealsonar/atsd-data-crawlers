package com.axibase.crawler.atsd;


public class PriceStatistics {
    public final int productId;

    public final double lastPrice;

    public final double lastDiscount;

    public PriceStatistics(int productId, double lastPrice, double lastDiscount) {
        this.productId = productId;
        this.lastPrice = lastPrice;
        this.lastDiscount = lastDiscount;
    }
}
