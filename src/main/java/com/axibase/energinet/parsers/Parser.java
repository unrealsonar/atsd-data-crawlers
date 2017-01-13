package com.axibase.energinet.parsers;

public interface Parser<T, S> {
    T parse(S paramS);
}