package com.axibase.crawler;

import com.axibase.crawler.data.CsvCategoryRow;
import com.axibase.crawler.data.FredCategory;
import com.axibase.crawler.data.FredSeries;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
class FredCategoryCrawler implements AutoCloseable {

    private static final String[] HEADERS = new String[] {"category_id","category_name","parent_id","parent_name","root_id","root_name","path"};
    private static final String[] HEADERS_WITH_SERIES_ID = new String[] {"series_id", "category_id","category_name","parent_id","parent_name","root_id", "root_name","path"};
    private static final String[] HEADERS_WITH_SERIES = new String[] {"series_id", "category_id","category_name","parent_id","parent_name","root_id","root_name","path"
                                , "frequency", "frequency_short", "notes", "observation_start", "observation_end", "popilarity", "group_popularity"
                                , "seasonal_adjustment", "seasonal_adjustment_short", "title", "units", "units_short"};

    private final FredClient client;
    private final String targetDirectory;
    private final CsvWriter mainWriter;
    private CsvWriter nestedWriter;
    private String nestedFileName;

    @Setter(AccessLevel.PACKAGE)
    private boolean withSeriesId;

    private boolean withSeries;

    FredCategoryCrawler(FredClient client, String targetDirectory) throws IOException {
        FileWriter fw = new FileWriter(targetDirectory + "all_categories.csv");
        this.mainWriter = new CsvWriter(fw, new CsvWriterSettings());

        if (withSeries) {
            mainWriter.writeRow(HEADERS_WITH_SERIES);
        } else if (withSeriesId) {
            mainWriter.writeRow(HEADERS_WITH_SERIES_ID);
        } else{
            mainWriter.writeHeaders(HEADERS);
        }

        this.client = client;
        this.targetDirectory = targetDirectory;
    }

    void setWithSeries(boolean withSeries) {
        this.withSeriesId = withSeries;
        this.withSeries = withSeries;
    }

    private void initNestedWriter(Integer rootId) throws IOException {
        this.nestedFileName = targetDirectory + "nested_categories_for_" + rootId + ".csv";
        FileWriter fw = new FileWriter(nestedFileName);
        this.nestedWriter = new CsvWriter(fw, new CsvWriterSettings());
        if (withSeries) {
            nestedWriter.writeRow(HEADERS_WITH_SERIES);
        } else if (withSeriesId) {
            nestedWriter.writeRow(HEADERS_WITH_SERIES_ID);
        } else{
            nestedWriter.writeHeaders(HEADERS);
        }
    }

    private void writeRow(Object[] rowData) {
        mainWriter.writeRow(rowData);
        nestedWriter.writeRow(rowData);
    }

    @Override
    public void close() {
        mainWriter.close();
    }

    private void flush() {
        mainWriter.flush();
    }

    private void closeNestedWriter() {
        if (nestedWriter != null) {
            nestedWriter.close();
        }
    }

    public void readAndWriterCategories(List<Integer> categoryIds) throws IOException {
        List<FredCategory> rootCategories = client.getRootCategories(categoryIds);
        log.info("Root categories: {}", getIds(rootCategories));
        for (FredCategory root : rootCategories) {
            log.info("Fetching root #{}", root.getId());
            initNestedWriter(root.getId());
            try {
                final CsvCategoryRow row = new CsvCategoryRow();
                row.setCategory(root);
                row.setParentCategoryName("Categories");
                row.setRoot(FredCategory.CATEGORIES);
                if (withSeriesId) {
                    log.info("Fetching seriess for #{} ...", root.getId());
                    List<FredSeries> seriess = client.getCategorySeries(root.getId());
                    seriess.forEach(s -> {
                        CsvCategoryRow copyRow = new CsvCategoryRow(row);
                        copyRow.setSeries(s);
                        mainWriter.writeRow(withSeries ? copyRow.getDataWithSeries() : copyRow.getData());
                    });
                    log.info("... finished[{}]", seriess.size());
                } else {
                    mainWriter.writeRow(row.getData());
                }
                getAndWriteNestedCategories(root, root, root.getName());
                System.out.println(MessageFormatter.format("File for category #{}: {}", root.getId(), nestedFileName).getMessage());
            } finally {
                flush();
                closeNestedWriter();
            }
        }
    }

    private void getAndWriteNestedCategories(FredCategory parent, FredCategory root, String path) {
        log.info("Fetching subs for #{}", parent.getId());
        FredCategory[] subs = client.getSubCategories(parent.getId());
        for (FredCategory sub : subs) {
            final CsvCategoryRow row = new CsvCategoryRow();
            row.setCategory(sub);
            row.setParentCategoryName(parent.getName());
            row.setRoot(root);
            row.addPathNode(path);
            if (withSeriesId) {
                log.info("Fetching seriess for #{} ...", sub.getId());
                List<FredSeries> seriess = client.getCategorySeries(sub.getId());
                seriess.forEach(s -> {
                    CsvCategoryRow copyRow = new CsvCategoryRow(row);
                    copyRow.setSeries(s);
                    writeRow(withSeries ? copyRow.getDataWithSeries() : copyRow.getData());
                });
                log.info("... finished[{}]", seriess.size());
            } else {
                writeRow(row.getData());
            }
            getAndWriteNestedCategories(sub, root, row.getPath());
        }
    }

    private static List<Integer> getIds(List<FredCategory> categories) {
        List<Integer> result = new ArrayList<>(categories.size());
        categories.forEach(c -> result.add(c.getId()));
        return result;
    }
}
