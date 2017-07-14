package com.axibase.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

class BoiParser implements Iterable<BoiSeries>, Iterator<BoiSeries> {
    private final Logger logger = LoggerFactory.getLogger(BoiParser.class);

    private static final int NBSP_CHARACTER = 160;

    private int pageCount = 0;
    private int currentPage = 1;
    private List<BoiSeries> currentSeries = new LinkedList<>();

    BoiParser() {
        currentPage = 1;
        pageCount = getTotalPages();
        parsePage();
    }

    private Document getDocumentAtPage(int page) throws IOException {
        Connection conn = Jsoup.connect("http://www.boi.org.il/en/DataAndStatistics/" +
                "_layouts/boi/handlers/WebPartHandler.aspx");

        conn.data("wp", "SeriesSearch")
                .data("lang", "en-US")
                .data("PaggingSize", "10")
                .data("textOrCode", "")
                .data("frequency", "0")
                .data("priceBase", "0")
                .data("season", "0")
                .data("economicSector", "0")
                .data("range", "0")
                .data("attach", "0")
                .data("interestType", "0")
                .data("investmentType", "0")
                .data("webUrl", "/en/DataAndStatistics")
                .data("page", String.valueOf(page));

        return conn.get();
    }

    private int getTotalPages() {
        logger.info("Fetching total pages");
        Document document = null;
        try {
            document = getDocumentAtPage(1);
        } catch (IOException e) {
            logger.error("Can't get document");
            e.printStackTrace();
            System.exit(1);
        }

        String boiPagesText = document.select("#BoipagesContainer").text();
        int lastNbsp = boiPagesText.lastIndexOf(NBSP_CHARACTER);
        int afterPreviousNbsp = boiPagesText.lastIndexOf(NBSP_CHARACTER, lastNbsp - 1) + 1;
        String countSubstring = boiPagesText.substring(afterPreviousNbsp + 1, lastNbsp).trim();
        return Integer.valueOf(countSubstring);
    }

    private void parsePage() {
        Document document = null;
        try {
            document = getDocumentAtPage(currentPage);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Can't get document");
            return;
        }

        logger.info("Reading page #" + currentPage);

        Elements additionalData = document.select("div.additionalData");
        for (Element element : additionalData) {
            Elements inputs = element.select("input");
            if (!inputs.isEmpty()) {
                BoiSeries series = new BoiSeries();
                for (Element input : inputs) {
                    String key = input.attr("class");
                    String value = input.attr("value");
                    switch (key) {
                        case "hidSeriesCode":
                            series.setCode(value);
                            break;
                        case "hidSeriesSubjectLevel":
                            series.setLevel(value);
                            break;
                        case "hidSeriesSubjectID":
                            series.setSubjectID(value);
                            break;
                        case "hidOutpuFreq":
                            series.setFrequency(value);
                            break;
                    }
                }
                currentSeries.add(series);
            }
        }
    }

    @Override
    public Iterator<BoiSeries> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return !(currentPage == pageCount && currentSeries.isEmpty());
    }

    @Override
    public BoiSeries next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (currentSeries.isEmpty()) {
            currentPage++;
            parsePage();
        }
        return currentSeries.remove(0);
    }
}
