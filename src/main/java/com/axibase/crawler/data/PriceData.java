package com.axibase.crawler.data;

import com.sun.istack.internal.Nullable;

public class PriceData {
    public final int id;

    public final String zone;

    @Nullable
    public final Double price;

    @Nullable
    public final Double discount;

    public PriceData(
            int id,
            String zone,
            @Nullable Double price,
            @Nullable Double discount) {
        this.id = id;
        this.zone = zone;
        this.price = price;
        this.discount = discount;
    }
}
