package com.axibase.energinet.extractors;

import com.axibase.tsd.model.data.series.Sample;
import com.axibase.tsd.network.InsertCommand;
import com.axibase.tsd.network.PlainCommand;
import org.slf4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

public class CommandExtractor implements Extractor<Collection<PlainCommand>, String[][]> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CommandExtractor.class);
    private Map<String, Map<String, Map<String, String>>> metricDescription;

    public CommandExtractor(Map<String, Map<String, Map<String, String>>> metricDescription) {
        this.metricDescription = metricDescription;
    }

    public Collection<PlainCommand> extract(String[][] table) {
        List<PlainCommand> commands = new ArrayList<PlainCommand>();
        int height = table.length;
        int width = table[0].length;


        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));


        for (int r = 2; r < height; r++) {
            long date = format.parse(table[r][0], new java.text.ParsePosition(0)).getTime() / 1000L;
            date += (Long.parseLong(table[r][1]) - 1L) * 3600L;
            date *= 1000L;

            String DEFAULT_ENTITY = "energinet.dk";

            for (int c = 2; c < width; c++) {
                PlainCommand plainCommand = null;
                if ((!org.apache.commons.lang3.StringUtils.isBlank(table[r][c])) && (!table[r][c].equals(" "))) {
                    String metric = table[0][c];
                    String name = table[1][c];
                    String value = table[r][c];
                    try {
                        Map<String, String> tags = this.metricDescription.get(metric).get(name);

                        metric = tags.get("metric").replace(' ', '_').trim();
                        Map<String, String> commandTags = new HashMap<String, String>();
                        for (Map.Entry<String, String> tag : tags.entrySet()) {
                            String tagName = tag.getKey();
                            if ((!tagName.equals("metric")) && (!tagName.equals("unit"))) {
                                commandTags.put(tagName, tag.getValue());
                            }
                        }
                        plainCommand = new InsertCommand(DEFAULT_ENTITY, metric, new Sample(date, Double.parseDouble(value)), commandTags);
                    } catch (Exception e) {
                        plainCommand = null;
                    }
                }

                if (plainCommand != null) {
                    commands.add(plainCommand);
                }
            }
        }
        log.info("Exatracted commands: {}", commands.size());
        return commands;
    }
}