package com.axibase.irscrawler;

import org.apache.commons.cli.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        Crawler crawler = new Crawler();

        Options cliOptions = new Options();
        cliOptions.addOption("d", true, "Start date yyyy-MM-dd");

        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        try {
            cmd = parser.parse(cliOptions, args);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        try {
            if (cmd.hasOption('d')) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = simpleDateFormat.parse(cmd.getOptionValue('d'));
                crawler.process(startDate);
            } else {
                crawler.process();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
