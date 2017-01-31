package com.axibase.energinet;

import com.axibase.energinet.browser.EnerginetGrabber;
import com.axibase.energinet.config.Config;
import com.axibase.energinet.extractors.CommandExtractor;
import com.axibase.energinet.parsers.MetricDescriptionParser;
import com.axibase.energinet.parsers.TableParser;
import com.axibase.energinet.sender.PartCommandSender;
import com.axibase.energinet.utils.Utils;
import com.axibase.tsd.client.ClientConfigurationFactory;
import com.axibase.tsd.client.DataService;
import com.axibase.tsd.client.HttpClientManager;
import com.axibase.tsd.model.system.ClientConfiguration;
import com.axibase.tsd.network.PlainCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class EnerginetTask extends TimerTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnerginetTask.class);
    private static Config config;
    private final TableParser tableParser;
    private final CommandExtractor commandExtractor;
    private final PartCommandSender partCommandSender;

    private EnerginetTask(Properties properties) {
        this.tableParser = new TableParser();
        MetricDescriptionParser metricDescriptionParser = new MetricDescriptionParser();
        Map<String, Map<String, Map<String, String>>> metricDescription = metricDescriptionParser.parse(
                Utils.fileAsString(config.getConfMetricsPath())
        );
        this.commandExtractor = new CommandExtractor(metricDescription, config.getDefaultEntity());
        this.partCommandSender = new PartCommandSender(getDataService());
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            EnerginetTask.config = Config.getInstance(args[0]);
        } else {
            throw new IllegalStateException("Incorrect number of args");
        }
    }

    private DataService getDataService() {
        ClientConfigurationFactory configurationFactory = new ClientConfigurationFactory(
                config.getProtocol(),
                config.getHost(),
                config.getPort(),
                "/api/v1",
                "/api/v1",
                config.getCredentials().getLogin(),
                config.getCredentials().getPassword(),
                3000,
                100000,
                600000L,
                true,
                false
        );
        ClientConfiguration clientConfiguration = configurationFactory.createClientConfiguration();
        HttpClientManager httpClientManager = new HttpClientManager(clientConfiguration);
        return new DataService(httpClientManager);
    }

    private String generateFileName(Boolean temporary) {
        return temporary ?
                String.format("%s/marketdata.xls", config.getDownloadDirectory()) :

                String.format("%s/marketdata_%s.xls", config.getDownloadDirectory(), new SimpleDateFormat("yyyy-MM-dd_HH-mm")
                        .format(new Date())
                );
    }

    private Date monthAgoDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(2, -1);
        return cal.getTime();
    }

    @Override
    public void run() {
        String temporaryFileName = generateFileName(true);
        try (EnerginetGrabber grabber = new EnerginetGrabber(config.getPhantomExecPath())) {
            grabber.grab(temporaryFileName, monthAgoDate(), new Date());
            String uniFileName = generateFileName(false);
            Utils.fileRename(temporaryFileName, uniFileName);
            String[][] table = this.tableParser.parse(Utils.fileAsString(uniFileName));
            Collection<PlainCommand> commands = this.commandExtractor.extract(table);
            this.partCommandSender.setPartSize(commands.size());
            this.partCommandSender.send(commands);
        }

    }
}