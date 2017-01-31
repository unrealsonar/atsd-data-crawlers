package com.axibase.energinet.extractors;

import com.axibase.tsd.model.data.series.Sample;
import com.axibase.tsd.network.InsertCommand;
import com.axibase.tsd.network.PlainCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommandExtractor implements Extractor<Collection<PlainCommand>, String[][]> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CommandExtractor.class);
    private final String defaultEntity;
    private Map<String, Map<String, Map<String, String>>> metricDescription;

    public CommandExtractor(Map<String, Map<String, Map<String, String>>> metricDescription, String defaultEntity) {
        this.metricDescription = metricDescription;
        this.defaultEntity = defaultEntity;
    }

    public Collection<PlainCommand> extract(String[][] table) {
        List<PlainCommand> commands = new ArrayList<>();
        int height = table.length;
        int width = table[0].length;


        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));


        for (int r = 2; r < height; r++) {
            long date = format.parse(table[r][0], new ParsePosition(0)).getTime() / 1000L;
            date += (Long.parseLong(table[r][1]) - 1L) * 3600L;
            date *= 1000L;

            for (int c = 2; c < width; c++) {
                PlainCommand plainCommand = null;
                if ((!StringUtils.isBlank(table[r][c])) && (!" ".equals(table[r][c]))) {
                    String metric = table[0][c];
                    String name = table[1][c];
                    String value = table[r][c];
                    Map<String, String> tags = this.metricDescription.get(metric).get(name);

                    metric = tags.get("metric").replace(' ', '_').trim();
                    Map<String, String> commandTags = new HashMap<>();
                    for (Map.Entry<String, String> tag : tags.entrySet()) {
                        String tagName = tag.getKey();
                        if ((!"metric".equals(tagName)) && (!"unit".equals(tagName))) {
                            commandTags.put(tagName, tag.getValue());
                        }
                    }
                    plainCommand = new InsertCommand(this.defaultEntity, metric, new Sample(date, Double.parseDouble(value)), commandTags);
                }
                if (plainCommand != null) {
                    commands.add(plainCommand);
                }
            }
        }
        LOGGER.info("Extracted commands: {}", commands.size());
        return commands;
    }

    public String getDefaultEntity() {
        return defaultEntity;
    }
}