package com.axibase.crawler.common;

public class Config {
    public final String host;
    public final int queryPort;
    public final int tcpPort;
    public final String user;
    public final String password;
    public final String ordersDirectory;
    public final String[] baseUrls;

    public Config(
            String host,
            int queryPort,
            int tcpPort,
            String user,
            String password,
            String ordersDirectory,
            String[] baseUrls) {
        this.host = host;
        this.queryPort = queryPort;
        this.tcpPort = tcpPort;
        this.user = user;
        this.password = password;
        this.ordersDirectory = ordersDirectory;
        this.baseUrls = baseUrls;
    }
}
