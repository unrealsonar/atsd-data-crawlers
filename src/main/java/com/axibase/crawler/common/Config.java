package com.axibase.crawler.common;

public class Config {
    public final String queryHost;

    public final String tcpHost;

    public final int tcpPort;

    public final String user;

    public final String password;

    public final String ordersDirectory;

    public final String[] baseUrls;

    public Config(
            String queryHost,
            String tcpHost,
            int tcpPort,
            String user,
            String password,
            String ordersDirectory,
            String[] baseUrls) {
        this.queryHost = queryHost;
        this.tcpHost = tcpHost;
        this.tcpPort = tcpPort;
        this.user = user;
        this.password = password;
        this.ordersDirectory = ordersDirectory;
        this.baseUrls = baseUrls;
    }
}
