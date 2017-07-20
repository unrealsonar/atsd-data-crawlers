package com.axibase.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static FredClient client = new FredClient();

    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        long year2016 = sdf.parse("2016-01-01").getTime();

        List<Integer> cats = withNestedCategories(Arrays.asList(1, 2, 9, 10));

        Set<FredSeries> allSeries = new HashSet<>();

        for (int cat : cats) {
            logger.info("Fetching category #" + cat);
            allSeries.addAll(Arrays.asList(client.categorySeries(cat)));
        }

        AtsdWriter w = new AtsdWriter("commands.txt");

        for (FredSeries series : allSeries) {
            logger.info(series.getId());
        }

        int cnt = 0;
        for (FredSeries series : allSeries) {
            long endTime = sdf.parse(series.getObservationEnd()).getTime();
            if (endTime < year2016)
                continue;

            cnt++;

            FredCategory[] seriesCats = client.seriesCategories(series.getId());
            int maxCatIndex = 0;
            for (int i = 0; i < seriesCats.length; i++) {
                if (seriesCats[maxCatIndex].getParentId() < seriesCats[i].getParentId()) {
                    maxCatIndex = i;
                }
            }
            FredCategory maxCategory = seriesCats[maxCatIndex];
            FredCategory parentCategory = client.getCategory(maxCategory.getParentId());
            String[] tags = client.seriesTags(series.getId());
            FredObservation[] observations = client.seriesObservations(series.getId());

            w.writeFredSeries(series, maxCategory, parentCategory, tags, observations);

            System.out.printf("%d of %d\n", cnt, allSeries.size());
        }
    }

    private static List<Integer> withNestedCategories(List<Integer> catIds) {
        Set<Integer> foundCategories = new HashSet<>(catIds);
        List<Integer> discoveryQueue = new LinkedList<>(catIds);

        while (!discoveryQueue.isEmpty()) {
            int category = discoveryQueue.remove(0);

            logger.info("Fetching subs #" + category);
            int[] subs = client.subCategories(category);
            for (int sub : subs) {
                if (!foundCategories.contains(sub)) {
                    foundCategories.add(sub);
                    discoveryQueue.add(sub);
                }
            }
        }

        return new ArrayList<>(foundCategories);
    }
}
