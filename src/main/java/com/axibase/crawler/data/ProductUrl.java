package com.axibase.crawler.data;

public class ProductUrl {
    public final int id;

    public final String name;

    public final String relativeUrl;

    public ProductUrl(int id, String name, String relativeUrl) {
        this.id = id;
        this.name = name;
        this.relativeUrl = relativeUrl;
    }
}
