package com.axibase.crawler.common;

/**
 * Created by alexey on 17.01.17.
 */
public class Result<E> {

    public final String errorText;

    public final E result;

    public Result(String errorText, E result) {
        this.errorText = errorText;
        this.result = result;
    }
}
