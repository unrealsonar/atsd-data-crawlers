package com.axibase.selenium;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SocrataMaker {

    private static File logFile = new File("SocrataMaker.log");

    public static void log(String msg) {
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss").format(Calendar.getInstance().getTime());
            FileUtils.writeStringToFile(logFile, timeStamp + ": " + msg + "\n", true);
        } catch (Exception ignored) {
        }
    }

    public static void logRefresh() {
        try {
            PrintWriter out = new PrintWriter(logFile.getAbsoluteFile());
            out.print("");
        } catch (Exception ignored) {
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        logRefresh();

        log("Initializing...");

        //load properties
        String dataPropertiesFile = "src/main/resources/data.properties";
        String urlPropertiesFile = "src/main/resources/url.properties";

        final Properties settingsProperties = new Properties();
        try {
            FileInputStream f = new FileInputStream(dataPropertiesFile);
            settingsProperties.load(f);
        } catch (Exception e) {
            log(String.format("property file %1s not found in %2s", dataPropertiesFile, System.getProperty("user.dir")));
        }

        final Properties urlProperties = new Properties();
        try {
            FileInputStream f = new FileInputStream(urlPropertiesFile);
            urlProperties.load(f);
        } catch (Exception e) {
            log(String.format("property file %1s not found in %2s", urlPropertiesFile, System.getProperty("user.dir")));
        }

        final Set<String> urls = urlProperties.stringPropertyNames();

        final ConcurrentLinkedQueue<String> urlsToProcess = new ConcurrentLinkedQueue<>(urls);
        final AtomicInteger remainingUrlsCount = new AtomicInteger(urls.size());

        int threadsCount;
        try {
            threadsCount = Integer.parseInt(settingsProperties.getProperty("threadCount"));
        } catch (Exception e) {
            log(e.getMessage());
            return;
        }
        final String collector = settingsProperties.getProperty("collector");
        final String username = settingsProperties.getProperty("username");
        final String password = settingsProperties.getProperty("password");
        final String jobName = settingsProperties.getProperty("jobName");

        List<Callable<DatasetSummaryInfoCollection>> tasks = new ArrayList<>(threadsCount);
        for (int i = 0; i < threadsCount; i++) {

            tasks.add(new Callable<DatasetSummaryInfoCollection>() {

                @Override
                public DatasetSummaryInfoCollection call() throws Exception {

                    Crawler crawler = new Crawler();

                    try {

                        crawler.init(
                                collector,
                                username,
                                password,
                                jobName,
                                urlProperties
                        );
                        List<DatasetSummaryInfo> infos = new ArrayList<>();
                        String currentUrl = urlsToProcess.poll();

                        while (currentUrl != null) {

                            DatasetSummaryInfo info = crawler.process(currentUrl);
                            if (info == null) {
                                log(String.format("Error processing url %1s", currentUrl));
                                currentUrl = urlsToProcess.poll();

                                System.out.println(String.format("Urls remaining: %1s", remainingUrlsCount.decrementAndGet()));

                                continue;
                            }

                            infos.add(info);
                            currentUrl = urlsToProcess.poll();

                            System.out.println(String.format("Urls remaining: %1s", remainingUrlsCount.decrementAndGet()));
                        }

                        return new DatasetSummaryInfoCollection(infos);

                    } catch (Exception ex) {
                        log(ex.getMessage());
                        return null;
                    } finally {
                        crawler.close();
                    }
                }

            });
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);
        List<Future<DatasetSummaryInfoCollection>> result = executorService.invokeAll(tasks);
        executorService.shutdown();

        List<DatasetSummaryInfo> datasetInfos = new ArrayList<>();
        for (Future<DatasetSummaryInfoCollection> future : result) {

            DatasetSummaryInfoCollection collection;

            try {
                collection = future.get();
            } catch (ExecutionException ex) {
                log(ex.getMessage());
                continue;
            }

            if (collection == null || collection.infos == null) continue;

            datasetInfos.addAll(collection.infos);
        }

        HashMap<String, ArrayList<DatasetSummaryInfo>> infosByHost = new HashMap<>();
        for (DatasetSummaryInfo info : datasetInfos) {

            ArrayList<DatasetSummaryInfo> infosCollection = infosByHost.get(info.host);
            if (infosCollection != null) {
                infosCollection.add(info);
            } else {
                infosCollection = new ArrayList<>();
                infosCollection.add(info);
                infosByHost.put(info.host, infosCollection);
            }
        }

        File file = new File("reports/README.md");

        if (!file.exists()) {

            File parentFile = file.getParentFile();
            parentFile.mkdirs();

            file.createNewFile();
        }

        try (PrintWriter writer = new PrintWriter(file)) {

            for (Map.Entry<String, ArrayList<DatasetSummaryInfo>> infoByHost : infosByHost.entrySet()) {
                writer.println(String.format("## %1s", infoByHost.getKey()));
                writer.println();
                writer.println("Name | Category | Updated");
                writer.println("---- | -------- | -------");

                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                for (DatasetSummaryInfo datasetInfo : infoByHost.getValue()) {

                    Date rowsUpdateDate = null;
                    try {
                        rowsUpdateDate = inputDateFormat.parse(datasetInfo.rowsUpdatedTime);
                    } catch (Exception ex) {
                        log(ex.getMessage());
                    }

                    writer.println(
                            String.format("[%1s](%2s) | %3s | %4s",
                                    datasetInfo.name,
                                    datasetInfo.descriptionFilePath,
                                    datasetInfo.category != null ? datasetInfo.category : "",
                                    rowsUpdateDate != null ? outputDateFormat.format(rowsUpdateDate) : ""));
                }

                writer.println();
            }

        } catch (Exception ex) {
            log(ex.getMessage());
        }
    }

    private static class DatasetSummaryInfoCollection {

        public final List<DatasetSummaryInfo> infos;

        public DatasetSummaryInfoCollection(List<DatasetSummaryInfo> infos) {
            this.infos = infos;
        }

    }
}
