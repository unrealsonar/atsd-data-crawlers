package com.axibase.crawler.data;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CsvCategoryRow {

    private int categoryId;
    private String categoryName;

    private int parentCategoryId;

    @Setter
    private String parentCategoryName;

    private Integer rootId;
    private String rootName;

    private List<String> pathNodes = new ArrayList<>();
    private String path;
    private Object[] data;

    public void setCategory(FredCategory category) {
        this.categoryId = category.getId();
        this.categoryName = category.getName();
        this.parentCategoryId = category.getParentId();
    }

    public void setRoot(FredCategory category) {
        this.rootId = category.getId();
        this.rootName = category.getName();
    }

    public void addPathNode(String node) {
        pathNodes.add(node);
    }

    public String getPath() {
        if (path == null) {
            pathNodes.add(categoryName);
            path = StringUtils.join(pathNodes, " > ");
        }
        return path;
    }

    public Object[] getData() {
        if (data == null) {
            data = new Object[]{categoryId, categoryName, parentCategoryId, parentCategoryName, rootId, rootName, getPath()};
        }
        return data;
    }
}
