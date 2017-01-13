
package com.axibase.energinet.parsers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class MetricDescriptionParser
        implements Parser<Map<String, Map<String, Map<String, String>>>, String> {
    private static final Logger log = LoggerFactory.getLogger(MetricDescriptionParser.class);
    private ObjectMapper mapper;

    public MetricDescriptionParser() {
        this.mapper = new ObjectMapper();
    }

    public Map<String, Map<String, Map<String, String>>> parse(String jsonInString) {
        try {
            return this.mapper.readValue(jsonInString, new TypeReference<Map<String, Map<String, Map<String, String>>>>() {
            });
        } catch (IOException e) {
            log.error("Failed to parse metric description: {}", jsonInString);
            throw new IllegalStateException("Failed to deserialize metric description", e);
        }
    }
}