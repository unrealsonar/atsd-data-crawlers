package com.axibase.crawler;

import com.axibase.tsd.client.ClientConfigurationFactory;
import com.axibase.tsd.client.HttpClientManager;
import com.axibase.tsd.model.system.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            Config config = Config.fromFile();
            System.getProperties()
                    .setProperty("axibase.tsd.api.client.properties", "client.properties");
            ClientConfiguration clientConfiguration =
                    ClientConfigurationFactory.createInstance().createClientConfiguration();
            HttpClientManager httpClientManager = new HttpClientManager(clientConfiguration);
            FredClient client = new FredClient(config.getApiKey());
            FredLoader loader = new FredLoader(config, client, httpClientManager);

            loader.runLoading();

            List<String> newSeries = loader.getUpdatedSeries(),
                    updatedSeries = loader.getNewSeries();
            logger.info("Updated series {}", updatedSeries);
            logger.info("New series {} ", newSeries);
            logger.info("Total updated series {}", updatedSeries.size());
            logger.info("Total new series {}", newSeries.size());
        } catch (Exception e) {
            logger.error("Error while loading or writing data", e);
        }
    }
}
