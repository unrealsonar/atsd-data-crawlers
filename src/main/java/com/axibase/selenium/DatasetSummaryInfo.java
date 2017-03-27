package com.axibase.selenium;

public class DatasetSummaryInfo {
    final String id;
    final String host;
    final String name;
    final String descriptionFilePath;
    final String category;
    final String publicationDate;
    final String firstSeriesCommand;

    public DatasetSummaryInfo(
            String id,
            String host,
            String name,
            String descriptionFilePath,
            String category,
            String publicationDate, String firstSeriesCommand) {
        this.id = id;
        this.host = host;
        this.name = name;
        this.descriptionFilePath = descriptionFilePath;
        this.category = category;
        this.publicationDate = publicationDate;
        this.firstSeriesCommand = firstSeriesCommand;
    }
}
