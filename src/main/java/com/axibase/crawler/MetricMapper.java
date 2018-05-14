package com.axibase.crawler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

class MetricMapper {
    private ObjectMapper fieldMapper;

    private boolean autoInclude = true;

    private String nameField;
    private String labelField;
    private String descriptionField;
    private String unitsField;

    private Set<String> excludedTagFields = new HashSet<>();
    private Map<String, List<String>> tagMapping = new HashMap<>();

    private MetricMapper(ObjectMapper fieldMapper) {
        this.autoInclude = true;
        this.fieldMapper = fieldMapper;
    }

    MetricMapper() {
        this(new ObjectMapper());
    }

    MetricMapper setNameField(String nameField) {
        this.nameField = nameField;
        return this;
    }

    MetricMapper setLabelField(String labelField) {
        this.labelField = labelField;
        return this;
    }

    MetricMapper setDescriptionField(String descriptionField) {
        this.descriptionField = descriptionField;
        return this;
    }

    MetricMapper setUnitsField(String unitsField) {
        this.unitsField = unitsField;
        return this;
    }

    MetricMapper setTagAutoInclude(boolean autoInclude) {
        this.autoInclude = autoInclude;
        return this;
    }

    MetricMapper excludeFromTags(String field) {
        excludedTagFields.add(field);
        return this;
    }

    MetricMapper addFieldToTagMapping(String field, String mapping) {
        if (!tagMapping.containsKey(field)) {
            tagMapping.put(field, new ArrayList<>());
        }
        tagMapping.get(field).add(mapping);
        return this;
    }

    void mapIntoBuilder(Object obj, MetricBuilder metricBuilder) {
        Map<String, Object> mappedObject =
                fieldMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});

        if (nameField != null)
            metricBuilder.setName(mappedObject.get(nameField));

        if (labelField != null)
            metricBuilder.setLabel(mappedObject.get(labelField));

        if (descriptionField != null)
            metricBuilder.setDescription(mappedObject.get(descriptionField));

        if (unitsField != null)
            metricBuilder.setUnits(mappedObject.get(unitsField));

        for (Map.Entry<String, Object> entry : mappedObject.entrySet()) {
            if (!excludedTagFields.contains(entry.getKey()) && autoInclude) {
                metricBuilder.setTag(entry.getKey(), entry.getValue());
            }
            if (tagMapping.containsKey(entry.getKey())) {
                List<String> renames = tagMapping.get(entry.getKey());
                for (String rename : renames) {
                    metricBuilder.setTag(rename, entry.getValue());
                }
            }
        }
    }
}
