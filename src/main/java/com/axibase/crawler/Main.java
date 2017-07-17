package com.axibase.crawler;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int RUN_COUNT = 5;

    public static void main(String[] args) {
        try {
            Set<BoiSeries> seriesSet = new HashSet<>();
            Set<String> seriesNameSet = new HashSet<>();

            // Try few times, because each time we have different data on the page
            for (int i = 1; i <= RUN_COUNT; i++) {
                logger.info("Try #" + i);
                BoiParser parser = new BoiParser();
                for (BoiSeries series : parser) {
                    seriesSet.add(series);
                    seriesNameSet.add(series.getCode());
                }
                logger.info("Total " + seriesSet.size() + " series now.");
                logger.info("Total " + seriesNameSet.size() + " series now.");
            }

            BoiToAtsdWriter writer = new BoiToAtsdWriter();
            for (BoiSeries series : seriesSet) {
                Document boiDocument = series.fetchSeriesDocument();
                writer.writeBoiDocument(boiDocument);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }
}
