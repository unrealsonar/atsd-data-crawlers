package com.axibase.crawler;

import com.axibase.tsd.client.ClientConfigurationFactory;
import com.axibase.tsd.client.HttpClientManager;
import com.axibase.tsd.model.system.ClientConfiguration;

public class Main {

    public static void main(String[] args) throws Exception {
        Config config = Config.fromFile();
        System.getProperties()
                .setProperty("axibase.tsd.api.client.properties", "client.properties");
        ClientConfiguration clientConfiguration =
                ClientConfigurationFactory.createInstance().createClientConfiguration();
        HttpClientManager httpClientManager = new HttpClientManager(clientConfiguration);
        FredClient client = new FredClient(config.getApiKey());
        FredLoader loader = new FredLoader(config, client, httpClientManager);
        loader.runLoading();
    }
}
