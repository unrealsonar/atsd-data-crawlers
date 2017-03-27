package com.axibase.selenium;

import com.google.common.collect.TreeMultiset;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

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
        final String username = settingsProperties.getProperty("collector_username");
        final String password = settingsProperties.getProperty("collector_password");
        final String jobName = settingsProperties.getProperty("jobName");

        ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);

        for (int i = 0; i < threadsCount; i++) {

            executorService.execute(new Runnable() {

                @Override
                public void run() {

                    Crawler crawler = new Crawler();

                    try {

                        crawler.init(
                                collector,
                                username,
                                password,
                                jobName,
                                urlProperties
                        );

                        String currentUrl = urlsToProcess.poll();

                        while (currentUrl != null) {

                            boolean success = crawler.process(currentUrl);
                            if (!success) {
                                System.out.println(String.format("Error processing url %1s", currentUrl));
                                currentUrl = urlsToProcess.poll();

                                System.out.println(String.format("Urls remaining: %1s", remainingUrlsCount.decrementAndGet()));

                                continue;
                            }

                            currentUrl = urlsToProcess.poll();

                            System.out.println(String.format("Urls remaining: %1s", remainingUrlsCount.decrementAndGet()));
                        }

                    } catch (Exception ex) {
                        log(ex.getMessage());
                    } finally {
                        crawler.close();
                    }
                }

            });
        }

        executorService.shutdown();

        // 30 sec per URL
        executorService.awaitTermination(urls.size() * 30, TimeUnit.SECONDS);

        List<DatasetSummaryInfo> infos;
        try {
            infos = getDatasetsInfo();
        } catch (IOException ex) {
            System.out.println(String.format("Unable to read dataset files. %s", ex.getMessage()));
            return;
        }

        try {
            writeTableOfContents(infos);
        } catch (IOException ex) {
            System.out.println(String.format("Unable to write TOC file. %s", ex.getMessage()));
            return;
        }

        try {
            writeCommandsFile(infos);
        } catch (IOException ex) {
            System.out.println(String.format("Unable to write commands file. %s", ex.getMessage()));
            return;
        }
    }

    private static List<DatasetSummaryInfo> getDatasetsInfo() throws IOException {
        File datasetsFolder = new File("reports/datasets/");
        File[] datasetFiles = datasetsFolder.listFiles();
        List<DatasetSummaryInfo> datasetInfos = new ArrayList<>(datasetFiles.length);

        for (File datasetFile : datasetFiles) {
            try (FileReader fileReader = new FileReader(datasetFile)) {
                try (BufferedReader reader = new BufferedReader(fileReader)) {

                    String id = null;
                    String host = null;
                    String name = null;
                    String category = null;
                    String publicationDate = null;
                    String firstSeriesCommand = null;

                    String currentLine = null;

                    // reading dataset section
                    while (true) {
                        currentLine = reader.readLine();

                        if (currentLine == null || currentLine.startsWith("## Description")) {
                            break;
                        }

                        if (currentLine.startsWith("| Id |")) {
                            String[] rowElements = currentLine.split("\\|");
                            if (rowElements.length < 2) continue;

                            id = rowElements[2].substring(1, rowElements[2].length() - 1);
                            continue;
                        }

                        if (currentLine.startsWith("| Host |")) {
                            String[] rowElements = currentLine.split("\\|");
                            if (rowElements.length < 2) continue;

                            host = rowElements[2].substring(1, rowElements[2].length() - 1);
                            continue;
                        }

                        if (currentLine.startsWith("| Name |") && !currentLine.equals("| Name | Value |")) {
                            String[] rowElements = currentLine.split("\\|");
                            if (rowElements.length < 2) continue;

                            name = rowElements[2].substring(1, rowElements[2].length() - 1);
                            continue;
                        }

                        if (currentLine.startsWith("| Category |")) {
                            String[] rowElements = currentLine.split("\\|");
                            if (rowElements.length < 2) continue;

                            category = rowElements[2].substring(1, rowElements[2].length() - 1);
                            continue;
                        }

                        if (currentLine.startsWith("| Publication Date |")) {
                            String[] rowElements = currentLine.split("\\|");
                            if (rowElements.length < 2) continue;

                            publicationDate = rowElements[2].substring(1, rowElements[2].length() - 1);
                            continue;
                        }
                    }

                    // searching for data commands block
                    while (true) {
                        currentLine = reader.readLine();

                        if (currentLine == null || currentLine.startsWith("## Data Commands")) {
                            break;
                        }
                    }

                    // searching for first series command
                    while (true) {
                        currentLine = reader.readLine();

                        if (currentLine == null || currentLine.startsWith("series")) {
                            break;
                        }
                    }

                    // reached EOF
                    if (currentLine == null) {
                        datasetInfos.add(new DatasetSummaryInfo(
                                id,
                                host,
                                name,
                                "datasets/" + datasetFile.getName(),
                                category,
                                publicationDate,
                                null));

                        continue;
                    }

                    StringBuilder commandBuilder = null;

                    while (currentLine != null) {
                        int quotesCount = StringUtils.countMatches(currentLine, "\"");

                        if (commandBuilder == null) {
                            commandBuilder = new StringBuilder(currentLine);

                            // single-line command
                            if (quotesCount % 2 == 0) {
                                break;
                            }

                            currentLine = reader.readLine();
                            continue;
                        }

                        if (quotesCount % 2 != 0) {
                            commandBuilder.append(currentLine);
                            currentLine = reader.readLine();
                            continue;
                        }

                        commandBuilder.append(currentLine);
                        break;
                    }

                    firstSeriesCommand = commandBuilder.toString();

                    datasetInfos.add(new DatasetSummaryInfo(
                            id, host,
                            name,
                            "datasets/" + datasetFile.getName(),
                            category,
                            publicationDate,
                            firstSeriesCommand));
                }
            }
        }

        return datasetInfos;
    }

    private static void writeTableOfContents(List<DatasetSummaryInfo> datasetInfos) throws IOException {

        // using sorted collections for alphabetic sorting hosts and datasets inside them
        Comparator<DatasetSummaryInfo> datasetSummaryInfoComparator = new Comparator<DatasetSummaryInfo>() {
            @Override
            public int compare(DatasetSummaryInfo o1, DatasetSummaryInfo o2) {
                return o1.name.compareTo(o2.name);
            }
        };

        TreeMap<String, TreeMultiset<DatasetSummaryInfo>> infosByHost = new TreeMap<>();
        for (DatasetSummaryInfo info : datasetInfos) {

            TreeMultiset<DatasetSummaryInfo> infosCollection = infosByHost.get(info.host);
            if (infosCollection != null) {
                infosCollection.add(info);
            } else {
                infosCollection = TreeMultiset.create(datasetSummaryInfoComparator);
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

            for (Map.Entry<String, TreeMultiset<DatasetSummaryInfo>> infoByHost : infosByHost.entrySet()) {
                writer.println(String.format("## %1s", infoByHost.getKey()));
                writer.println();
                writer.println("Name | Category | Published");
                writer.println("---- | -------- | ---------");

                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                for (DatasetSummaryInfo datasetInfo : infoByHost.getValue()) {

                    Date publicationDate = null;
                    try {
                        publicationDate = inputDateFormat.parse(datasetInfo.publicationDate);
                    } catch (Exception ex) {
                        log(ex.getMessage());
                    }

                    writer.println(
                            String.format("[%1s](%2s) | %3s | %4s",
                                    datasetInfo.name,
                                    datasetInfo.descriptionFilePath,
                                    datasetInfo.category != null ? datasetInfo.category : "",
                                    publicationDate != null ? outputDateFormat.format(publicationDate) : ""));
                }

                writer.println();
            }

        } catch (Exception ex) {
            log(ex.getMessage());
        }

    }

    private static void writeCommandsFile(List<DatasetSummaryInfo> datasetInfos) throws IOException {
        File file = new File("reports/series-commands.md");

        if (!file.exists()) {

            File parentFile = file.getParentFile();
            parentFile.mkdirs();

            file.createNewFile();
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            for (DatasetSummaryInfo info : datasetInfos) {
                writer.println(String.format("[%s.md](%s)",
                        info.id,
                        info.descriptionFilePath));

                writer.println();
                writer.println("```ls");
                writer.println(info.firstSeriesCommand);
                writer.println("```");
                writer.println();
                writer.println();
            }
        }
    }
}
