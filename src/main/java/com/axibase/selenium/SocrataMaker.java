package com.axibase.selenium;

import com.google.common.collect.TreeMultiset;
import com.sun.istack.internal.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;

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
                    ArrayList<String> tags = null;

                    String currentLine = null;

                    // reading dataset section
                    while (true) {
                        currentLine = reader.readLine();

                        if (currentLine == null ||
                                currentLine.startsWith("## Description") ||
                                currentLine.startsWith("## Columns")) {
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

                        if (currentLine.startsWith("| Tags |")) {
                            String[] rowElements = currentLine.split("\\|");
                            if (rowElements.length < 2) continue;

                            tags = new ArrayList<>(
                                    Arrays.asList(
                                            rowElements[2]
                                                    .substring(1, rowElements[2].length() - 1)
                                                    .split(", |; ")));

                            if (tags.size() > 0) {
                                String lastTag = tags.get(tags.size() - 1);
                                if (lastTag != null && lastTag.endsWith("...")) {
                                    tags.remove(tags.size() - 1);
                                }
                            }
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
                                "../datasets/" + datasetFile.getName(),
                                category,
                                publicationDate,
                                null,
                                tags));

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
                            "../datasets/" + datasetFile.getName(),
                            category,
                            publicationDate,
                            firstSeriesCommand,
                            tags));
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

        TreeMap<String, TreeMultiset<DatasetSummaryInfo>> infosByCategory = new TreeMap<>();
        TreeMap<String, TreeMultiset<DatasetSummaryInfo>> infosByHost = new TreeMap<>();

        for (DatasetSummaryInfo info : datasetInfos) {
            String category = StringUtils.isNotEmpty(info.category) ? info.category : "Other";
            TreeMultiset<DatasetSummaryInfo> infosCollection = infosByCategory.get(category);
            if (infosCollection != null) {
                infosCollection.add(info);
            } else {
                infosCollection = TreeMultiset.create(datasetSummaryInfoComparator);
                infosCollection.add(info);
                infosByCategory.put(category, infosCollection);
            }

            infosCollection = infosByHost.get(info.host);
            if (infosCollection != null) {
                infosCollection.add(info);
            } else {
                infosCollection = TreeMultiset.create(datasetSummaryInfoComparator);
                infosCollection.add(info);
                infosByHost.put(info.host, infosCollection);
            }
        }

        HashMap<String, String> categoryIds = new HashMap<>();
        HashSet<Integer> existingCategoryIds = new HashSet<>(infosByCategory.size());

        for (Map.Entry<String, TreeMultiset<DatasetSummaryInfo>> infoByCategory : infosByCategory.entrySet()) {
            int categoryId = infoByCategory.getKey().hashCode();
            while (existingCategoryIds.contains(categoryId)) {
                categoryId++;
            }
            existingCategoryIds.add(categoryId);
            categoryIds.put(infoByCategory.getKey(), Integer.toHexString(categoryId));
        }

        TreeMap<String, TreeMultiset<DatasetSummaryInfo>> filteredInfoByTags = filterByTags(datasetInfos, datasetSummaryInfoComparator);
        HashMap<String, String> tagIds = new HashMap<>(filteredInfoByTags.size());
        HashSet<Integer> existingTagIds = new HashSet<>(filteredInfoByTags.size());

        for (Map.Entry<String, TreeMultiset<DatasetSummaryInfo>> infoByTag : filteredInfoByTags.entrySet()) {
            int tagId = infoByTag.getKey().hashCode();
            while (existingTagIds.contains(tagId)) {
                tagId++;
            }
            existingTagIds.add(tagId);
            tagIds.put(infoByTag.getKey(), Integer.toHexString(tagId));
        }

        File file = new File("reports/README.md");

        if (!file.exists()) {
            File parentFile = file.getParentFile();
            parentFile.mkdirs();

            file.createNewFile();
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("# Hosts");
            writer.println();
            writer.println("Name | Datasets count");
            writer.println("---- | --------------");

            for (Map.Entry<String, TreeMultiset<DatasetSummaryInfo>> infoByHost : infosByHost.entrySet()) {
                writer.println(
                        String.format(
                                "[%1$s](data-hosts/%1$s.md) | %2$s",
                                infoByHost.getKey(),
                                infoByHost.getValue().size()));
            }

            writer.println();

            writer.println("# Categories");
            writer.println();
            writer.println("Name | Datasets count");
            writer.println("---- | --------------");

            for (Map.Entry<String, TreeMultiset<DatasetSummaryInfo>> infoByCategory : infosByCategory.entrySet()) {
                writer.println(String.format(
                        "[%s](data-categories/%s.md) | %s",
                        infoByCategory.getKey(),
                        categoryIds.get(infoByCategory.getKey()),
                        infoByCategory.getValue().size()));
            }

            writer.println();

            writer.println("# Most commonly used tags");
            writer.println();
            writer.println("Name | Datasets count");
            writer.println("---- | --------------");

            for (Map.Entry<String, TreeMultiset<DatasetSummaryInfo>> infoByTag : filteredInfoByTags.entrySet()) {
                writer.println(String.format(
                        "[%s](data-tags/%s.md) | %s",
                        infoByTag.getKey(),
                        tagIds.get(infoByTag.getKey()),
                        infoByTag.getValue().size()));
            }
        }

        writeHostGroupedFiles(infosByHost);
        writeCategoryGroupedFiles(infosByCategory, categoryIds);
        writeTagGroupedFiles(filteredInfoByTags, tagIds);
    }

    private static TreeMap<String, TreeMultiset<DatasetSummaryInfo>> filterByTags(
            List<DatasetSummaryInfo> datasetInfos,
            Comparator<DatasetSummaryInfo> datasetSummaryInfoComparator) {
        HashMap<String, Integer> allFilteredTags = new HashMap<>();

        for (DatasetSummaryInfo info : datasetInfos) {
            if (info.tags != null) {
                for (String tag : info.tags) {
                    String filteredTag = filterTag(tag);
                    if (filteredTag == null) continue;

                    Integer count = allFilteredTags.get(filteredTag);
                    if (count != null) {
                        count++;
                        allFilteredTags.put(filteredTag, count);
                    } else {
                        allFilteredTags.put(filteredTag, 1);
                    }
                }
            }
        }

        HashMap<String, String> pluralToSingularReplacementTable = new HashMap<>(allFilteredTags.size());
        for (String tag : allFilteredTags.keySet()) {
            String tagInPlural = English.plural(tag);
            pluralToSingularReplacementTable.put(tagInPlural, tag);
        }

        HashMap<String, String> exceptions = new HashMap<>();
        exceptions.put("data", "data");

        for (Map.Entry<String, String> exception : exceptions.entrySet()) {
            pluralToSingularReplacementTable.put(exception.getKey(), exception.getValue());
        }

        TreeMap<String, TreeMultiset<DatasetSummaryInfo>> infoByTags = new TreeMap<>();
        for (DatasetSummaryInfo info : datasetInfos) {
            if (info.tags != null) {
                for (String tag : info.tags) {
                    if (tag == null || !allFilteredTags.containsKey(tag)) continue;
                    String singularTag = pluralToSingularReplacementTable.get(tag);
                    if (singularTag == null) singularTag = tag;

                    TreeMultiset<DatasetSummaryInfo> infosCollection = infoByTags.get(singularTag);
                    if (infosCollection != null) {
                        infosCollection.add(info);
                    } else {
                        infosCollection = TreeMultiset.create(datasetSummaryInfoComparator);
                        infosCollection.add(info);
                        infoByTags.put(singularTag, infosCollection);
                    }
                }
            }
        }

        TreeMap<String, TreeMultiset<DatasetSummaryInfo>> filteredInfoByTags = new TreeMap<>();
        for (Map.Entry<String, TreeMultiset<DatasetSummaryInfo>> infoByTag : infoByTags.entrySet()) {
            if (infoByTag.getValue().size() < 10) continue;
            filteredInfoByTags.put(infoByTag.getKey(), infoByTag.getValue());
        }

        return filteredInfoByTags;
    }

    @Nullable
    private static String filterTag(String tag) {
        if (tag == null || tag.length() == 0) return null;

        if (!Character.isLetter(tag.charAt(0))) return null;

        String[] words = tag.split(" ");
        if (words.length == 0) return null;

        String firstWord = words[0];
        if (!StringUtils.isAlphanumeric(firstWord)) return null;

        return firstWord;
    }

    private static void writeCategoryGroupedFiles(
            TreeMap<String, TreeMultiset<DatasetSummaryInfo>> infosByCategory,
            HashMap<String, String> categoryIds) throws IOException {
        for (Map.Entry<String, TreeMultiset<DatasetSummaryInfo>> infoByCategory : infosByCategory.entrySet()) {
            File categoryDatasetsFile = new File(String.format("reports/data-categories/%s.md", categoryIds.get(infoByCategory.getKey())));
            if (!categoryDatasetsFile.exists()) {
                File parentFile = categoryDatasetsFile.getParentFile();
                parentFile.mkdirs();

                categoryDatasetsFile.createNewFile();
            }
            try (PrintWriter writer = new PrintWriter(categoryDatasetsFile)) {
                writer.println(String.format("# %1s", infoByCategory.getKey()));
                writer.println();
                writer.println("Name | Host | Published");
                writer.println("---- | ---- | ---------");

                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                for (DatasetSummaryInfo datasetInfo : infoByCategory.getValue()) {

                    Date publicationDate = null;
                    try {
                        publicationDate = inputDateFormat.parse(datasetInfo.publicationDate);
                    } catch (Exception ex) {
                        log(ex.getMessage());
                    }

                    writer.println(
                            String.format("[%s](%s) | %s | %s",
                                    datasetInfo.name,
                                    datasetInfo.descriptionFilePath,
                                    datasetInfo.host,
                                    publicationDate != null
                                            ? outputDateFormat.format(publicationDate).replace("-", "&#x2011;")
                                            : ""));
                }

                writer.println();
            } catch (Exception ex) {
                log(ex.getMessage());
            }
        }
    }

    private static void writeTagGroupedFiles(
            TreeMap<String, TreeMultiset<DatasetSummaryInfo>> infosByTags,
            HashMap<String, String> tagIds) throws IOException {
        for (Map.Entry<String, TreeMultiset<DatasetSummaryInfo>> infoByTag : infosByTags.entrySet()) {
            File tagDatasetsFile = new File(String.format("reports/data-tags/%s.md", tagIds.get(infoByTag.getKey())));
            if (!tagDatasetsFile.exists()) {
                File parentFile = tagDatasetsFile.getParentFile();
                parentFile.mkdirs();

                tagDatasetsFile.createNewFile();
            }
            try (PrintWriter writer = new PrintWriter(tagDatasetsFile)) {
                writer.println(String.format("# %1s", infoByTag.getKey()));
                writer.println();
                writer.println("Name | Host | Published");
                writer.println("---- | ---- | ---------");

                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                for (DatasetSummaryInfo datasetInfo : infoByTag.getValue()) {
                    Date publicationDate = null;
                    try {
                        publicationDate = inputDateFormat.parse(datasetInfo.publicationDate);
                    } catch (Exception ex) {
                        log(ex.getMessage());
                    }

                    writer.println(
                            String.format("[%s](%s) | %s | %s",
                                    datasetInfo.name,
                                    datasetInfo.descriptionFilePath,
                                    datasetInfo.host,
                                    publicationDate != null
                                            ? outputDateFormat.format(publicationDate).replace("-", "&#x2011;")
                                            : ""));
                }

                writer.println();
            } catch (Exception ex) {
                log(ex.getMessage());
            }
        }
    }

    private static void writeHostGroupedFiles(TreeMap<String, TreeMultiset<DatasetSummaryInfo>> infosByHost) throws IOException {
        for (Map.Entry<String, TreeMultiset<DatasetSummaryInfo>> infoByHost : infosByHost.entrySet()) {
            File hostDatasetsFile = new File(String.format("reports/data-hosts/%s.md", infoByHost.getKey()));
            if (!hostDatasetsFile.exists()) {
                File parentFile = hostDatasetsFile.getParentFile();
                parentFile.mkdirs();

                hostDatasetsFile.createNewFile();
            }
            try (PrintWriter writer = new PrintWriter(hostDatasetsFile)) {
                writer.println(String.format("# %1s", infoByHost.getKey()));
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
                            String.format("[%s](%s) | %s | %s",
                                    datasetInfo.name,
                                    datasetInfo.descriptionFilePath,
                                    datasetInfo.category != null ? datasetInfo.category : "",
                                    publicationDate != null
                                            ? outputDateFormat.format(publicationDate).replace("-", "&#x2011;")
                                            : ""));
                }

                writer.println();
            } catch (Exception ex) {
                log(ex.getMessage());
            }
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
                writer.println(formatCommand(info.firstSeriesCommand));
                writer.println("```");
                writer.println();
            }
        }
    }

    private static String formatCommand(String command) {
        if (command == null) return null;

        StringBuilder formattedCommand = new StringBuilder();
        // splitting all metrics and tags to a new string
        String[] commandParts = command.split("(?= [t,m]:)");

        for (String commandPart : commandParts) {
            String[] limitedLengthParts = commandPart.split("(?<=\\G.{108})");

            for (String limitedCommandPart : limitedLengthParts) {
                formattedCommand.append(limitedCommandPart);
                formattedCommand.append("\n");
            }
        }

        return formattedCommand.toString();
    }
}
