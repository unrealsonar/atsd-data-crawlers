package com.axibase.crawler.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FredCategory {

    public static final FredCategory CATEGORIES = createBase();

    private int id;
    private String name;

    @JsonProperty("parent_id")
    private int parentId;

    private static FredCategory createBase() {
        FredCategory category = new FredCategory();
        category.setId(0);
        category.setName("Categories");
        return category;
    }

}
