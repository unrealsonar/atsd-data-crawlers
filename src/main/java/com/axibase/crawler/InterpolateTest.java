package com.axibase.crawler;

import com.axibase.tsd.client.ClientConfigurationFactory;
import com.axibase.tsd.client.DataService;
import com.axibase.tsd.client.HttpClientManager;
import com.axibase.tsd.client.MetaDataService;
import com.axibase.tsd.model.data.command.AddSeriesCommand;
import com.axibase.tsd.model.data.series.Sample;
import com.axibase.tsd.model.meta.Entity;
import com.axibase.tsd.model.meta.Interpolate;
import com.axibase.tsd.model.meta.Metric;
import com.axibase.tsd.model.system.ClientConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.Time;
import java.util.TimeZone;

public class InterpolateTest {
    public static void main(String[] args) throws JsonProcessingException {
        System.getProperties()
                .setProperty("axibase.tsd.api.client.properties", "test-client.properties");

        ClientConfiguration clientConfiguration =
                ClientConfigurationFactory.createInstance().createClientConfiguration();
        HttpClientManager httpClientManager = new HttpClientManager(clientConfiguration);

        DataService dataService = new DataService(httpClientManager);
        MetaDataService metaDataService = new MetaDataService(httpClientManager);

        Metric met = new Metric();
        met.setName("met-abc");
        met.setInterpolate(Interpolate.PREVIOUS);
        met.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        metaDataService.createOrReplaceMetric(met);

        Entity ent = new Entity();
        ent.setInterpolate(Interpolate.PREVIOUS);
        ent.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        ent.setLabel("lbl");
        ent.setName("my-ent");
        metaDataService.createOrReplaceEntity(ent);

//        AddSeriesCommand addSeriesCommand = new AddSeriesCommand("ent123", "met-abc");
//        addSeriesCommand.addSeries(new Sample(1000, 123.4));
//        dataService.addSeries(addSeriesCommand);

        met = metaDataService.retrieveMetric("met-abc");
        ent = metaDataService.retrieveEntity("my-ent");
        System.out.println(met);
        System.out.println(ent);
    }
}
