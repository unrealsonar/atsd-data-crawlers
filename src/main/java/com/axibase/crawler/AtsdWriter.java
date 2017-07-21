package com.axibase.crawler;

import com.axibase.tsd.client.ClientConfigurationFactory;
import com.axibase.tsd.client.DataService;
import com.axibase.tsd.client.HttpClientManager;
import com.axibase.tsd.client.MetaDataService;
import com.axibase.tsd.model.data.command.AddSeriesCommand;
import com.axibase.tsd.model.data.series.Sample;
import com.axibase.tsd.model.meta.Metric;
import com.axibase.tsd.model.system.ClientConfiguration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class AtsdWriter {
    private static final String ENTITY_NAME = "fred.stlouisfed.org";

    private DataService dataService;
    private MetaDataService metaDataService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private BufferedWriter cmdOut;

    AtsdWriter(String commandsPath) throws IOException {
        cmdOut = new BufferedWriter(new FileWriter(commandsPath));

        dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        isoFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

        System.getProperties()
                .setProperty("axibase.tsd.api.client.properties", "client.properties");

        ClientConfiguration clientConfiguration =
                ClientConfigurationFactory.createInstance().createClientConfiguration();
        HttpClientManager httpClientManager = new HttpClientManager(clientConfiguration);

        dataService = new DataService(httpClientManager);
        metaDataService = new MetaDataService(httpClientManager);
    }

    void writeFredSeries(FredSeries fredSeries,
                         FredCategory cat, FredCategory parent,
                         String[] tags, FredObservation[] data) throws ParseException, IOException {
        Metric met = new Metric(fredSeries.getId());
        Map<String, String> mtags = new HashMap<>();
        met.setTags(mtags);

        StringBuilder metricCmdBuilder = new StringBuilder("metric");

        met.setName(fredSeries.getId());
        metricCmdBuilder.append(" m:").append(fredSeries.getId());
        met.setLabel(fredSeries.getTitle());
        metricCmdBuilder.append(" l:").append(quoteString(fredSeries.getTitle()));
        met.setDescription(fredSeries.getNotes());
        metricCmdBuilder.append(" d:").append(quoteString(fredSeries.getNotes()));
        met.setUnits(fredSeries.getUnits());
        metricCmdBuilder.append(" u:").append(quoteString(fredSeries.getUnits()));


        mtags.put("series_id", fredSeries.getId());
        metricCmdBuilder.append(" t:series_id=").append(quoteString(fredSeries.getId()));

        mtags.put("category", cat.getName());
        metricCmdBuilder.append(" t:category=").append(quoteString(cat.getName()));

        mtags.put("category_id", String.valueOf(cat.getId()));
        metricCmdBuilder.append(" t:category_id=").append(cat.getId());

        mtags.put("parent_category", parent.getName());
        metricCmdBuilder.append(" t:parent_category=").append(quoteString(parent.getName()));

        mtags.put("parent_category_id", String.valueOf(parent.getId()));
        metricCmdBuilder.append(" t:parent_category_id=").append(parent.getId());

        mtags.put("frequency", fredSeries.getFrequency());
        metricCmdBuilder.append(" t:frequency=").append(quoteString(fredSeries.getFrequency()));

        mtags.put("frequency_short", fredSeries.getFrequencyShort());
        metricCmdBuilder.append(" t:frequency_short=").append(quoteString(fredSeries.getFrequencyShort()));

        mtags.put("seasonal_adjustment", fredSeries.getSeasonalAdjustment());
        metricCmdBuilder.append(" t:seasonal_adjustment=").append(quoteString(fredSeries.getSeasonalAdjustment()));

        mtags.put("seasonal_adjustment_short", fredSeries.getSeasonalAdjustmentShort());
        metricCmdBuilder.append(" t:seasonal_adjustment_short=").append(quoteString(fredSeries.getSeasonalAdjustmentShort()));

        mtags.put("units", fredSeries.getUnits());
        metricCmdBuilder.append(" t:units=").append(quoteString(fredSeries.getUnits()));

        mtags.put("units_short", fredSeries.getUnitsShort());
        metricCmdBuilder.append(" t:units_short=").append(quoteString(fredSeries.getUnitsShort()));

        mtags.put("popularity", String.valueOf(fredSeries.getPopilarity()));
        metricCmdBuilder.append(" t:popularity=").append(fredSeries.getPopilarity());

        mtags.put("notes", fredSeries.getNotes());
        metricCmdBuilder.append(" t:notes=").append(quoteString(fredSeries.getNotes()));

        mtags.put("observation_start", fredSeries.getObservationStart());
        metricCmdBuilder.append(" t:observation_start=").append(quoteString(fredSeries.getObservationStart()));

        mtags.put("observation_end", fredSeries.getObservationEnd());
        metricCmdBuilder.append(" t:observation_end=").append(quoteString(fredSeries.getObservationEnd()));

        StringBuilder tagsBuilder = new StringBuilder();
        for (int i = 0; i < tags.length; i++) {
            if (i > 0) {
                tagsBuilder.append(',');
            }
            tagsBuilder.append(tags[i]);
        }
        String tagsString = tagsBuilder.toString();

        mtags.put("tags", tagsString);
        metricCmdBuilder.append(" t:tags=").append(quoteString(tagsString));
        metricCmdBuilder.append('\n');

        cmdOut.write(metricCmdBuilder.toString());
        metaDataService.createOrReplaceMetric(met);

        AddSeriesCommand addCommand = new AddSeriesCommand(ENTITY_NAME, fredSeries.getId());
        int k = 0;
        for (FredObservation observation : data) {
            long date = dateFormat.parse(observation.getDate()).getTime();
            if (date < 0)
                continue;

            String strValue = observation.getValue();
            Double value = null;

            boolean text = false;

            Sample observationSample;

            if (!strValue.equals(".")) {
                try {
                    value = Double.parseDouble(strValue);
                } catch (NumberFormatException e) {
                    text = true;
                }
            } else {
                strValue = "NaN";
            }

            if (!text) {
                observationSample = new Sample(date, value);
            } else {
                observationSample = new Sample(date, value, strValue);
            }

            addCommand.addSeries(observationSample);
            k++;

            String isoStringDate = isoFormat.format(new Date(date));

            String seriesCmd = String.format("series e:%s m:%s=%s d:%s\n",
                    ENTITY_NAME, fredSeries.getId(), strValue, isoStringDate);

            if (text)
                seriesCmd += String.format(" x:%s=%s", fredSeries.getId(), quoteString(strValue));

            cmdOut.write(seriesCmd);

            if (k == 100) {
                dataService.addSeries(addCommand);
                addCommand = new AddSeriesCommand(ENTITY_NAME, fredSeries.getId());
            }
        }
        if (k > 0) {
            dataService.addSeries(addCommand);
        }
        cmdOut.flush();
    }

    private String quoteString(String s) {
        if (s == null || s.isEmpty())
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
}
