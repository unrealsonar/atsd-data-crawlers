package com.axibase.crawler;

import com.axibase.crawler.common.Config;
import com.axibase.crawler.common.ConfigurationManager;
import com.axibase.crawler.common.Logger;

import org.apache.commons.cli.*;

public class Main {

    public static void main(String[] args) {
        Options cliOptions = new Options();
        cliOptions.addOption("rf", false, "Force full update");
        cliOptions.addOption("r", false, "Force refresh products");

        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        try {
            cmd = parser.parse(cliOptions, args);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        Config config;
        try {
            ConfigurationManager manager = new ConfigurationManager();
            config = manager.loadConfig();
        } catch (Exception ex) {
            Logger.log("Unable to load configuration file");
            return;
        }

        String productUrlsFile = "product-urls.properties";
        Crawler crawler = new Crawler();

        if (cmd.hasOption("rf")) {
            crawler.fullUpdate(config, productUrlsFile);
            return;
        }

        if (cmd.hasOption("r")){
            crawler.scanProducts(config, productUrlsFile);
            return;
        }

        crawler.scanPrices(config, productUrlsFile);
    }
}
