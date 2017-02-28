package com.axibase.crawler.common;

/**
 * Created by alexey on 20.01.17.
 */
public class Logger {

    public static void log(String message) {
        System.out.println(message);
    }

    public static void log(String message, Object... args) {
        System.out.println(String.format(message, args));
    }
}
