package com.axibase.selenium;

import java.util.List;

public class DatasetSummaryInfo {
    final String id;
    final String host;
    final String name;
    final String descriptionFilePath;
    final String category;
    final String publicationDate;
    final String firstSeriesCommand;
    final List<String> tags;

    public DatasetSummaryInfo(
            String id,
            String host,
            String name,
            String descriptionFilePath,
            String category,
            String publicationDate,
            String firstSeriesCommand,
            List<String> tags) {
        this.id = id;
        this.host = host;
        this.name = name;
        this.descriptionFilePath = descriptionFilePath;
        this.category = category;
        this.publicationDate = publicationDate;
        this.firstSeriesCommand = firstSeriesCommand;
        this.tags = tags;
    }
}
