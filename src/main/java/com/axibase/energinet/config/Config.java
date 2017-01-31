package com.axibase.energinet.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.axibase.energinet.config.Config.PropertyNames.*;


public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private static final String ENERGINET_DK = "energinet.dk";
    private static Config instance;
    private Credentials credentials;
    private String host;
    private String port;
    private String protocol;
    private String defaultEntity = ENERGINET_DK;
    private String phantomExecPath;
    private String downloadDirectory;
    private String confMetricsPath;
    private Integer interval;

    private Config() {
    }

    public static Config getInstance(String configPath) {
        if (instance == null) {
            instance = retrieveConfig(configPath);
        }
        return instance;
    }

    private static Config retrieveConfig(String configPath) {
        Properties properties = getProperties(configPath);
        Credentials credentials = new Credentials(
                properties.get(PropertyNames.ATSD_USER).toString(),
                properties.get(PropertyNames.ATSD_PASSWORD).toString()
        );
        return new Builder()
                .setCredentials(credentials)
                .setDefaultEntity(properties.get(DEFAULT_ENTITY).toString())
                .setPhantomExecPath(properties.get(PHANTOM_EXEC).toString())
                .setDownloadDirectory(properties.get(DOWNLOAD_DIRECTORY).toString())
                .setHost(properties.get(ATSD_HOST).toString())
                .setPort(properties.get(ATSD_PORT).toString())
                .setProtocol(properties.get(ATSD_PROTOCOL).toString())
                .setConfMetricPath(properties.get(CONF_METRICS).toString())
                .setInterval(Integer.parseInt(properties.get(INTERVAL).toString()))
                .build();
    }

    private static Properties getProperties(String propertiesFilePath) {
        try (FileInputStream fis = new FileInputStream(propertiesFilePath)) {
            Properties properties = new Properties();
            properties.load(fis);
            return properties;
        } catch (IOException e) {
            LOGGER.error("Failed to load properties file! {}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    public Credentials getCredentials() {
        return credentials;
    }

    private Builder builder() {
        return new Builder();
    }

    public String getDefaultEntity() {
        return defaultEntity;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getPhantomExecPath() {
        return phantomExecPath;
    }

    public String getDownloadDirectory() {
        return downloadDirectory;
    }

    public String getConfMetricsPath() {
        return confMetricsPath;
    }

    public Integer getInterval() {
        return interval;
    }

    enum PropertyNames {
        ATSD_USER("atsd.user"), ATSD_PASSWORD("atsd.password"), ATSD_PORT("atsd.port"),
        ATSD_HOST("atsd.host"), ATSD_PROTOCOL("atsd.protocol"), DOWNLOAD_DIRECTORY("download.directory"),
        DEFAULT_ENTITY("default.entity"), PHANTOM_EXEC("phantom.exec"), INTERVAL("interval.hour"),
        CONF_METRICS("conf.metrics");

        private String value;

        PropertyNames(String name) {
            this.value = name;
        }


        @Override
        public String toString() {

            return value;
        }
    }

    private static class Builder {
        private Config config;

        private Builder() {
            this.config = new Config();
        }

        private Builder setCredentials(Credentials credentials) {
            config.credentials = credentials;
            return this;
        }

        private Builder setDefaultEntity(String defaultEntity) {
            config.defaultEntity = defaultEntity;
            return this;
        }

        private Builder setPhantomExecPath(String phantomExecPath) {
            config.phantomExecPath = phantomExecPath;
            return this;
        }

        private Builder setHost(String host) {
            config.host = host;
            return this;
        }

        private Builder setProtocol(String protocol) {
            config.protocol = protocol;
            return this;
        }

        private Builder setPort(String port) {
            config.port = port;
            return this;
        }

        private Builder setDownloadDirectory(String downloadDirectory) {
            config.downloadDirectory = downloadDirectory;
            return this;
        }

        private Builder setConfMetricPath(String confMetricPath) {
            config.confMetricsPath = confMetricPath;
            return this;
        }

        private Builder setInterval(Integer interval) {
            config.interval = interval;
            return this;
        }

        private Config build() {
            return config;
        }
    }
}
