package com.axibase.crawler;

import com.axibase.crawler.util.CommandLineUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.axibase.crawler.util.CommandLineUtils.*;

@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {
        final CommandLine commandLine = CommandLineUtils.parseArguments(args);
        final String apiKey = commandLine.getOptionValue(OPT_API_KEY.getLongOpt());
        final List<Integer> categoryIds = toListOfInteger(commandLine.getOptionValues(OPT_IDS.getLongOpt()));
        String directory = commandLine.getOptionValue(OPT_DIR.getLongOpt(), "./");
        if (!StringUtils.endsWith(directory, File.separator)) {
            directory += File.separator;
        }

        System.out.println("Starting to read data...");
        final FredClient client = new FredClient(apiKey);

        try (FredCategoryCrawler crawler = new FredCategoryCrawler(client, directory)) {
            if (commandLine.hasOption(OPT_WITH_SERIES_ID.getLongOpt())) {
                crawler.setWithSeriesId(true);
            }
            if (commandLine.hasOption(OPT_WITH_SERIES.getLongOpt())) {
                crawler.setWithSeries(true);
            }
            crawler.readAndWriterCategories(categoryIds);
            System.out.print("...finished");
        } catch (Exception exc) {
            log.error("Error", exc);
        }
    }

    private static List<Integer> toListOfInteger(String[] strs) {
        if (strs.length == 0) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<>(strs.length);
        for (String str : strs) {
            result.add(Integer.parseInt(str));
        }
        return result;
    }

}
