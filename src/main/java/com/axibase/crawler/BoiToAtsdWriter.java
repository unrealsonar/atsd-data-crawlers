package com.axibase.crawler;

import com.axibase.tsd.client.ClientConfigurationFactory;
import com.axibase.tsd.client.DataService;
import com.axibase.tsd.client.HttpClientManager;
import com.axibase.tsd.client.MetaDataService;
import com.axibase.tsd.model.data.command.AddSeriesCommand;
import com.axibase.tsd.model.data.series.Sample;
import com.axibase.tsd.model.meta.Metric;
import com.axibase.tsd.model.system.ClientConfiguration;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class BoiToAtsdWriter {
    private final Logger logger = LoggerFactory.getLogger(BoiToAtsdWriter.class);

    private static final String ENTITY_NAME = "boi.org.il";

    private DataService dataService;
    private MetaDataService metaDataService;

    BoiToAtsdWriter() {
        System.getProperties()
                .setProperty("axibase.tsd.api.client.properties", "client.properties");

        ClientConfiguration clientConfiguration =
                ClientConfigurationFactory.createInstance().createClientConfiguration();
        HttpClientManager httpClientManager = new HttpClientManager(clientConfiguration);

        dataService = new DataService(httpClientManager);
        metaDataService = new MetaDataService(httpClientManager);
    }

    public void writeBoiDocument(Document document) {
        Elements tables = document.select("table");

        if (tables.size() < 2) {
            logger.error("Incorrect series document: should have 2 HTML tables");
            return;
        }

        String boiMetric = createMetricFromTable(tables.get(0).children().first());
        createSeriesFromTable(boiMetric, tables.get(1).children().first());
    }

    private String createMetricFromTable(Element metricTable) {
        Metric boiMetric = new Metric();
        Map<String, String> boiTags = new HashMap<>();
        boiMetric.setTags(boiTags);

        for (Element row : metricTable.children()) {
            Elements rowKeyValue = row.children();

            if (rowKeyValue.size() != 2) {
                logger.warn("Incorrect key/value pair");
            }

            String rowKey = rowKeyValue.get(0).text();
            String rowValue = cleanValue(rowKeyValue.get(1).text());

            switch (rowKey) {
                case "Code":
                    boiMetric.setName(rowValue);
                    boiMetric.setLabel(rowValue);
                    break;
                case "Description":
                    boiMetric.setDescription(rowValue);
                    break;
                case "Units":
                    boiMetric.setUnits(rowValue);
                    break;
                case "Data source":
                    boiTags.put("source", rowValue);
                    break;
                default:
                    String tagKey = rowKey.replace(" ", "").toLowerCase();
                    boiTags.put(tagKey, rowValue);
            }
        }

        metaDataService.createOrReplaceMetric(boiMetric);

        return boiMetric.getName();
    }

    private void createSeriesFromTable(String metricName, Element seriesTable) {
        int sampleCount = 0;
        AddSeriesCommand addCommand = new AddSeriesCommand(ENTITY_NAME, metricName);

        for (Element row : seriesTable.children()) {
            Elements dateValue = row.children();
            String stringDate = dateValue.get(0).text();
            String stringValue = dateValue.get(1).text();

            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            Date date;
            try {
                date = format.parse(stringDate);
            } catch (ParseException e) {
                e.printStackTrace();
                logger.error("Incorrect date format: '" + stringDate + "'; skipping");
                continue;
            }
            long time = date.getTime();

            if (time < 0) {
                logger.warn("Skipping negative date " + stringDate);
                continue;
            }

            double value = Double.parseDouble(stringValue);

            addCommand.addSeries(new Sample(time, value));
            sampleCount++;

            if (sampleCount >= 100) {
                sampleCount = 0;
                dataService.addSeries(addCommand);
                addCommand = new AddSeriesCommand(ENTITY_NAME, metricName);
            }
        }

        if (sampleCount > 0)
            dataService.addSeries(addCommand);
    }

    private String cleanValue(String value) {
        value = value.trim();
        boolean alpha = false, numeric = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            alpha |= Character.isAlphabetic(c);
            numeric |= Character.isDigit(c);
        }
        if (!alpha && !numeric)
            return "";
        return value;
    }
}
