package com.axibase.irscrawler.common;

import java.util.Date;

public class Series {
    public final Date date;
    public final String entity;
    public final String section;
    public final String type;
    public final int currentYearValue;
    public final int previousYearValue;

    public Series(
            Date date,
            String entity,
            String section,
            String type,
            int currentYearValue,
            int previousYearValue) {
        this.date = date;
        this.entity = entity;
        this.section = section;
        this.type = type;
        this.currentYearValue = currentYearValue;
        this.previousYearValue = previousYearValue;
    }
}
