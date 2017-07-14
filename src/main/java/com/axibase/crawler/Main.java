package com.axibase.crawler;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        int seriesNumber = 1;

        try {
            BoiParser parser = new BoiParser();
            BoiToAtsdWriter writer = new BoiToAtsdWriter();

            for (BoiSeries series : parser) {
                logger.info("Getting series #" + seriesNumber++);
                Document seriesDocument = series.fetchSeriesDocument();
                writer.writeBoiDocument(seriesDocument);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        logger.info("Finished, total " + seriesNumber + " metrics created");
    }
}
