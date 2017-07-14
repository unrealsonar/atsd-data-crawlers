package com.axibase.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class BoiSeries {
    private final Logger logger = LoggerFactory.getLogger(BoiSeries.class);

    private String code;
    private String level;
    private String subjectID;
    private String frequency;

    void setCode(String code) {
        this.code = code;
    }

    void setLevel(String level) {
        this.level = level;
    }

    void setSubjectID(String subjectID) {
        this.subjectID = subjectID;
    }

    void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    private Map<String, String> requestParameters() {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("SeriesCode", code);
        paramsMap.put("Level", level);
        paramsMap.put("Sid", subjectID);
        paramsMap.put("Freq", frequency);
        return paramsMap;
    }

    Document fetchSeriesDocument() {
        logger.info("Extracting document for: " + code);

        Connection connection = Jsoup.connect("http://www.boi.org.il/en/DataAndStatistics/Pages/boi.ashx");
        connection.data("Command", "DownloadSeriesExcel")
                .data(requestParameters())
                .ignoreContentType(true);

        Document document;
        try {
            document = connection.get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return document;
    }
}
