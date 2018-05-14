package com.axibase.crawler.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FredSeries {
    private String id;

    private String title;

    @JsonProperty("observation_start")
    private String observationStart;

    @JsonProperty("observation_end")
    private String observationEnd;

    private String frequency;

    @JsonProperty("frequency_short")
    private String frequencyShort;

    private String units;

    @JsonProperty("units_short")
    private String unitsShort;

    @JsonProperty("seasonal_adjustment")
    private String seasonalAdjustment;

    @JsonProperty("seasonal_adjustment_short")
    private String seasonalAdjustmentShort;

    private int popilarity;

    private String notes;

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // Identifier-only equality
        return (obj instanceof FredSeries) &&
                id.equals(((FredSeries) obj).id);
    }
}
