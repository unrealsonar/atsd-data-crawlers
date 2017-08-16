package com.axibase.crawler;

import com.axibase.crawler.data.CsvCategoryRow;
import com.axibase.crawler.data.FredCategory;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Slf4j
class FredCategoryCrawler implements AutoCloseable {

    private static final String[] HEADERS = new String[] {"category_id","category_name","parent_id","parent_name","root_id","root_name","path"};

    private final FredClient client;
    private final String targetDirectory;
    private final CsvWriter mainWriter;
    private CsvWriter nestedWriter;
    private String nestedFileName;

    FredCategoryCrawler(FredClient client, String targetDirectory) throws IOException {
        FileWriter fw = new FileWriter(targetDirectory + "categories.csv");
        this.mainWriter = new CsvWriter(fw, new CsvWriterSettings());
        mainWriter.writeHeaders(HEADERS);
        this.client = client;
        this.targetDirectory = targetDirectory;
    }

    private void initNestedWriter(Integer rootId) throws IOException {
        this.nestedFileName = targetDirectory + "categories_" + rootId + "_nested.csv";
        FileWriter fw = new FileWriter(nestedFileName);
        this.nestedWriter = new CsvWriter(fw, new CsvWriterSettings());
        nestedWriter.writeHeaders(HEADERS);
    }

    private void writeRow(CsvCategoryRow row) {
        mainWriter.writeRow(row.getData());
        nestedWriter.writeRow(row.getData());
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
        log.info("Root categories: {}", rootCategories);
        for (FredCategory root : rootCategories) {
            log.info("Fetching root #{}", root.getId());
            initNestedWriter(root.getId());
            try {
                CsvCategoryRow row = new CsvCategoryRow();
                row.setCategory(root);
                row.setParentCategoryName("Categories");
                row.setRoot(FredCategory.CATEGORIES);
                mainWriter.writeRow(row.getData());
                getAndWriteNestedCategories(root, root, root.getName());
                System.out.println(MessageFormatter.format("File for category #{}: {}", root.getId(), nestedFileName).getMessage());
            } finally {
                flush();
                closeNestedWriter();
            }
        }
    }

    private void getAndWriteNestedCategories(FredCategory parent, FredCategory root, String path) {
        log.info("Fetching subs #{}", parent.getId());
        FredCategory[] subs = client.subCategories(parent.getId());
        for (FredCategory sub : subs) {
            CsvCategoryRow row = new CsvCategoryRow();
            row.setCategory(sub);
            row.setParentCategoryName(parent.getName());
            row.setRoot(root);
            row.addPathNode(path);
            writeRow(row);
            getAndWriteNestedCategories(sub, root, row.getPath());
        }
    }

}
