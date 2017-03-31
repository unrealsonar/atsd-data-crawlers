package com.axibase.irscrawler.common;

public class Result<E> {

    public final String errorText;

    public final E result;

    public Result(String errorText, E result) {
        this.errorText = errorText;
        this.result = result;
    }
}
