package com.axibase.crawler.data;

import java.util.List;

public class ProductData {
    public final ProductInfo info;

    public final List<PriceData> priceData;

    public final String units;

    public final boolean isInLastOrder;

    public final double lastPrice;

    public final double lastDiscount;

    public ProductData(
            ProductInfo info,
            List<PriceData> priceData,
            String units,
            boolean isInLastOrder,
            double lastPrice, double lastDiscount) {
        this.info = info;
        this.priceData = priceData;
        this.units = units;
        this.isInLastOrder = isInLastOrder;
        this.lastPrice = lastPrice;
        this.lastDiscount = lastDiscount;
    }
}
