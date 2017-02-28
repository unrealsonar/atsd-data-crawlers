package com.axibase.crawler.data;

import java.util.Map;

public class ProductInfo {
    public final int id;
    public final String name;
    public final String url;
    public final String category;
    public final String subcategory;
    public final Map<String, String> attributes;

    public ProductInfo(
            int id,
            String name,
            String url,
            String category,
            String subcategory,
            Map<String, String> attributes) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.category = category;
        this.subcategory = subcategory;
        this.attributes = attributes;
    }
}
