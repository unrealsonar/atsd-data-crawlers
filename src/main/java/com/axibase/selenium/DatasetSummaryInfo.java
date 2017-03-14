package com.axibase.selenium;

public class DatasetSummaryInfo {

    final String host;

    final String name;

    final String descriptionFilePath;

    final String category;

    final String rowsUpdatedTime;

    public DatasetSummaryInfo(
            String host,
            String name,
            String descriptionFilePath,
            String category,
            String rowsUpdatedTime) {
        this.host = host;
        this.name = name;
        this.descriptionFilePath = descriptionFilePath;
        this.category = category;
        this.rowsUpdatedTime = rowsUpdatedTime;
    }
}
