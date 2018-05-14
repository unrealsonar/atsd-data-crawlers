package com.axibase.crawler;

import com.axibase.tsd.model.meta.Metric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MetricBuilder {
    private String name;
    private String label;
    private String units;
    private String description;
    private Map<String, String> tags = new HashMap<>();

    private String objectToString(Object obj) {
        if (obj == null)
            return "";

        if (obj instanceof List) {
            List list = (List) obj;

            StringBuilder respresentationBuilder = new StringBuilder();
            int index = 0;
            for (Object element : list) {
                if (index > 0) {
                    respresentationBuilder.append(',');
                }
                index++;

                respresentationBuilder.append(element);
            }
            return respresentationBuilder.toString();
        }

        return String.valueOf(obj);
    }

    private String quoteString(String s) {
        if (s.isEmpty())
            return "\"\"";

        boolean needQuote = false;
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '=' || c == '"' || Character.isWhitespace(c))
                needQuote = true;
            if (c == '"')
                sb.append("\"\"");
            else
                sb.append(c);
        }
        String result =  sb.toString();
        if (needQuote)
            result = '"' + result + '"';
        return result;
    }
    
    void setName(Object name) {
        this.name = objectToString(name);
    }
    
    void setLabel(Object label) {
        this.label = objectToString(label);
    }
    
    void setUnits(Object units) {
        this.units = objectToString(units);
    }
    
    void setDescription(Object description) {
        this.description = objectToString(description);
    }
    
    void setTag(String tagKey, Object tagValue) {
        this.tags.put(tagKey, objectToString(tagValue));
    }

    String toCommand() {
        StringBuilder commandBuilder = new StringBuilder("metric");

        commandBuilder.append(" m:").append(name);
        commandBuilder.append(" l:").append(label);
        commandBuilder.append(" d:").append(description);
        commandBuilder.append(" u:").append(units);

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            commandBuilder.append(" t:").append(entry.getKey()).append("=")
                    .append(quoteString(entry.getValue()));
        }

        return commandBuilder.toString();
    }

    Metric toMetric() {
        Metric metric = new Metric(name);
        metric.setLabel(label);
        metric.setUnits(units);
        metric.setDescription(description);
        metric.setTags(tags);

        return metric;
    }
}
