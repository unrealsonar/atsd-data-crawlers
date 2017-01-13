package com.axibase.energinet;

import com.axibase.energinet.browser.EnerginetGrabber;
import com.axibase.energinet.extractors.CommandExtractor;
import com.axibase.energinet.parsers.MetricDescriptionParser;
import com.axibase.energinet.parsers.TableParser;
import com.axibase.energinet.sender.PartCommandSender;
import com.axibase.energinet.utils.Utils;
import com.axibase.tsd.client.ClientConfigurationFactory;
import com.axibase.tsd.client.DataService;
import com.axibase.tsd.client.HttpClientManager;
import com.axibase.tsd.client.MetaDataService;
import com.axibase.tsd.model.system.ClientConfiguration;
import com.axibase.tsd.network.PlainCommand;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EnerginetTask extends TimerTask {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EnerginetTask.class);
    private static final String DOWNLOAD_PATH = "data/marketdata.xls";
    private EnerginetGrabber grabber;
    private TableParser tableParser;
    private CommandExtractor commandExtractor;
    private PartCommandSender partCommandSender;
    private String downloadPath;
    private String defaultEntity;
    private MetaDataService metaDataService;

    private EnerginetTask(Properties properties) {
        this.grabber = new EnerginetGrabber(properties.getProperty(properties.getProperty("phantom.exec")));
        this.tableParser = new TableParser();
        this.defaultEntity = properties.getProperty("default.entity");
        MetricDescriptionParser metricDescriptionParser = new MetricDescriptionParser();
        Map<String, Map<String, Map<String, String>>> metricDescription = metricDescriptionParser.parse(Utils.fileAsString(properties.getProperty("conf.metrics")));
        ClientConfigurationFactory configurationFactory = new ClientConfigurationFactory(
                properties.getProperty("atsd.protocol"),
                properties.getProperty("atsd.host"),
                properties.getProperty("atsd.port"),
                "/api/v1",
                "/api/v1",
                properties.getProperty("atsd.user"),
                properties.getProperty("atsd.password"),
                3000,
                100000,
                600000L,
                true,
                false
        );
        ClientConfiguration clientConfiguration = configurationFactory.createClientConfiguration();
        HttpClientManager httpClientManager = new HttpClientManager(clientConfiguration);
        DataService dataService = new DataService(httpClientManager);
        this.metaDataService = new MetaDataService(httpClientManager);
        this.commandExtractor = new CommandExtractor(metricDescription);
        this.partCommandSender = new PartCommandSender(dataService);
        this.downloadPath = properties.getProperty("download.directory");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            Properties properties = getProperties(args[0]);
            Long timerInterval = Integer.valueOf(properties.getProperty("interval.hour")) * 60 * 60 * 1000L;
            new Timer().schedule(new EnerginetTask(properties), new Date(), timerInterval);
        } else {
            throw new IllegalStateException("Incorrect number of args");
        }
    }

    private static Properties getProperties(String arg) {
        try (FileInputStream fis = new FileInputStream(arg)) {
            Properties properties = new Properties();
            properties.load(fis);
            return properties;
        } catch (IOException e) {
            String errorMessage = String.format(
                    "Failed to load properties file! %s",
                    e.getMessage()
            );
            throw new IllegalStateException(
            );
        }
    }

    private String generateFileName(Boolean temporary) {
        return temporary ?
                String.format("%s/marketdata.xls", this.downloadPath) :

                String.format("%s/marketdata_%s.xls", this.downloadPath, new SimpleDateFormat("yyyy-MM-dd_HH-mm")
                        .format(new Date())
                );
    }

    private Date monthAgoDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(2, -1);
        return cal.getTime();
    }

    public void run() {
        String temporaryFileName = generateFileName(true);
        this.grabber.grab(temporaryFileName, monthAgoDate(), new Date());
        String uniFileName = generateFileName(false);
        Utils.fileRename(temporaryFileName, uniFileName);
        String[][] table = this.tableParser.parse(Utils.fileAsString(uniFileName));
        Collection<PlainCommand> commands = this.commandExtractor.extract(table);
        this.partCommandSender.setPartSize(commands.size());
        this.partCommandSender.send(commands);
    }
}