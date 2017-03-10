package com.axibase.selenium;

/*
 * Created by boriss on 07.02.17.
 * parse some datasets from catalog.data.gov with some info via ATSD Collector
 */


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;

import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SocrataMaker {
    private static final int commandsNumber = 3;
    private static final int metacommandsNumber = 100;

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

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "phantomjs");
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
                new String[]{"--web-security=no", "--ignore-ssl-errors=yes"});

        //load properties
        Properties pr = new Properties();
        try {
            FileInputStream f = new FileInputStream("src/main/resources/data.properties");
            pr.load(f);
        } catch (Exception e) {
            System.out.println("property file is not loaded");
        }
        String collector = pr.getProperty("collector");
        String username = pr.getProperty("username");
        String password = pr.getProperty("password");
        String jobName = pr.getProperty("jobName");


        try {
            FileInputStream f = new FileInputStream("src/main/resources/url.properties");
            pr.load(f);
        } catch (Exception e) {
            System.out.println("property file is not loaded");
        }

        log("parsing finished");
        log("entering collector...");

        //authentication
        WebDriver driver = new PhantomJSDriver(caps);
        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        WebDriverWait wait = new WebDriverWait(driver, 1);

        driver.get(collector);
        WebElement field = driver.findElement(By.id("username"));
        field.sendKeys(username);
        field = driver.findElement(By.name("password"));
        field.sendKeys(password);
        field.submit();

        log("entered");

        String collectorMainPageUrl = driver.getCurrentUrl();

        Set<String> urls = pr.stringPropertyNames();
        ArrayList<DatasetSummaryInfo> datasetInfos = new ArrayList<>(urls.size());
        int limit = 100;

        for (String urlPropertyName : urls) {
            try {
                DatasetSummaryInfo info = processUrl(urlPropertyName, jobName, driver, wait, pr);

                if (info == null) {
                    driver.get(collectorMainPageUrl);
                    continue;
                }

                datasetInfos.add(info);
                limit--;
                System.out.println(limit);
                if (limit == 0) break;

            } catch (Exception ex) {
                log(ex.getMessage());
                driver.get(collectorMainPageUrl);
            }
        }

        HashMap<String, ArrayList<DatasetSummaryInfo>> infosByHost = new HashMap<>();
        for (DatasetSummaryInfo info : datasetInfos) {

            ArrayList<DatasetSummaryInfo> infosCollection = infosByHost.get(info.host);
            if (infosCollection != null) {
                infosCollection.add(info);
            }
            else {
                infosCollection = new ArrayList<>();
                infosCollection.add(info);
                infosByHost.put(info.host, infosCollection);
            }
        }

        try (PrintWriter writer = new PrintWriter("reports/README.md")) {

            for (Map.Entry<String, ArrayList<DatasetSummaryInfo>> infoByHost : infosByHost.entrySet()) {
                writer.println(String.format("## %1s", infoByHost.getKey()));
                writer.println();
                writer.println("Name | Category | Updated");
                writer.println("---- | -------- | -------");

                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                for (DatasetSummaryInfo datasetInfo : infoByHost.getValue()) {

                    Date rowsUpdateDate = null;
                    try
                    {
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

        driver.quit();
    }

    private static DatasetSummaryInfo processUrl(
            String urlPropertyName,
            String jobName,
            WebDriver driver,
            WebDriverWait wait,
            Properties pr) throws IOException, URISyntaxException {

        log("Adding new Socrata job...");

        WebElement link = driver.findElement(By.linkText("Jobs"));
        link.click();

        link = driver.findElement(By.linkText(jobName));
        link.click();

        link = driver.findElement(By.linkText("Create from URL"));
        link.click();
        WebElement field = driver.findElement(By.id("inputWizardUrl"));
        wait.until(ExpectedConditions.visibilityOf(field));
        field.sendKeys(urlPropertyName);

        link = driver.findElement(By.id("btnWizardCreate"));
        link.click();

        WebElement nameField = driver.findElement(By.id("name"));
        String check = nameField.getAttribute("value");

        if (check.equals("")) return null;

        //[dataset]
        DatasetMetadata datasetMetadata = getDatasetMetadata(driver, urlPropertyName, pr);

        //[columns]
        List<DatasetColumn> datasetColumns = getDatasetColumns(driver);

        log("parsing time...");

        //[time]
        field = driver.findElement(By.id("timeField_0"));
        String time = field.getAttribute("value");

        field = driver.findElement(By.id("timeFormat_0"));
        String format = field.getAttribute("value");

        log("parsing series...");

        //[series]
        field = driver.findElement(By.id("metricPrefix_0"));
        String prefix = field.getAttribute("value");

        field = driver.findElement(By.id("includedFields_0"));
        String included = field.getAttribute("value");

        field = driver.findElement(By.id("excludedFields_0"));
        String excluded = field.getAttribute("value");

        field = driver.findElement(By.id("annotationFields_0"));
        String annotation = field.getAttribute("value");

        log("parsing commands & meta-commands...");

        //[commands] & [meta-commands]
        ArrayList<String> commands = new ArrayList<>(commandsNumber);
        ArrayList<String> metacommands = new ArrayList<>(metacommandsNumber);

        WebElement element = driver.findElement(By.xpath("//*[@id=\"testRow_2\"]/td/table/tbody/tr[3]/td[2]"));
        String[] lines = element.getText().split("\n");

        for (String line : lines) {
            if (line.startsWith("series") && commands.size() < commandsNumber) {
                commands.add(line);
                continue;
            }

            if (line.startsWith("metric") ||
                    line.startsWith("entity") ||
                    line.startsWith("property")) {
                metacommands.add(line);
            }
        }

        log("writing in file...");

        //write in file
        File file = new File("reports/datasets/" + datasetMetadata.id + ".md");

        if (!file.exists()) {
            file.createNewFile();
        }
        try (PrintWriter writer = new PrintWriter(file.getAbsoluteFile())) {

            writeNameSection(datasetMetadata.name, writer);

            writeDatasetSection(datasetMetadata, writer);

            writeDescriptionSection(datasetMetadata.description, writer);

            writeColumnsSection(datasetColumns, writer);

            writeTimeFieldSection(time, format, writer);

            writeSeriesFieldSection(prefix, included, excluded, annotation, writer);

            writeDataCommandsSection(commands, writer);

            writeMetadataCommandsSection(metacommands, writer);
        }

        return new DatasetSummaryInfo(
                datasetMetadata.host,
                datasetMetadata.name,
                "datasets/" + datasetMetadata.id + ".md",
                datasetMetadata.category,
                datasetMetadata.rowsUpdatedDate);
    }

    private static DatasetMetadata getDatasetMetadata(WebDriver driver, String urlPropertyName, Properties pr) {
        String catalogUrl = pr.getProperty(urlPropertyName);

        int metadataLinkLength = urlPropertyName.indexOf("/rows");
        String metadataUrl = null;
        if (metadataLinkLength > 0) {
            metadataUrl = urlPropertyName.substring(0, metadataLinkLength);
        }

        String dataJsonUrl = urlPropertyName + "?max_rows=100";
        String dataCsvUrl = urlPropertyName.replace("/rows.json", "/rows.csv") + "?max_rows=100";

        String datasetHost = null;
        try {
            URI datasetUri = new URI(urlPropertyName);
            datasetHost = datasetUri.getHost();
        } catch (Exception ex) {
            log(ex.getMessage());
        }

        int datasetlen = driver.findElements(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr")).size();

        String name = null;
        String category = null;
        String rowsUpdatedDate = null;
        String description = null;
        String tags = null;
        String attribution = null;
        String id = null;
        String createdDate = null;
        String publicationDate = null;

        for (int i = 2; i <= datasetlen; i++) {
            WebElement nameElement = driver.findElement(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr[" + i + "]/td[1]"));
            String itemName = nameElement.getText();

            WebElement valueElement = driver.findElement(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr[" + i + "]/td[2]"));
            String itemValue = valueElement.getText();

            switch (itemName) {
                case "Description": {
                    description = getDescription(driver, i);
                    break;
                }
                case "Name": {
                    name = itemValue;
                    break;
                }
                case "Category": {
                    category = itemValue;
                    break;
                }
                case "Rows Updated": {
                    rowsUpdatedDate = itemValue;
                    break;
                }
                case "Attribution": {
                    attribution = itemValue;
                    break;
                }
                case "Id": {
                    id = itemValue;
                    break;
                }
                case "Created": {
                    createdDate = itemValue;
                    break;
                }
                case "Publication Date": {
                    publicationDate = itemValue;
                    break;
                }
                case "Tags" : {
                    tags = itemValue.replaceAll("[\\[\\]]","");
                }
            }
        }

        return new DatasetMetadata(
                catalogUrl,
                metadataUrl,
                dataJsonUrl,
                dataCsvUrl,
                datasetHost,
                id,
                name,
                description,
                tags,
                attribution,
                category,
                createdDate,
                publicationDate,
                rowsUpdatedDate
        );
    }

    private static List<DatasetColumn> getDatasetColumns(WebDriver driver) {
        WebElement columnsTable = driver.findElement(By.id("tblColumnInfos"));
        List<WebElement> rows = columnsTable.findElements(By.tagName("tr"));

        ArrayList<DatasetColumn> datasetColumns = new ArrayList<>(rows.size());

        // skip first two header rows
        for (int i = 2; i < rows.size(); i++) {
            WebElement row = rows.get(i);
            List<WebElement> parameters = row.findElements(By.tagName("td"));

            String name = parameters.get(0).getText();
            String fieldName = parameters.get(1).getText();
            String dataType = parameters.get(2).getText();
            String renderType = parameters.get(3).getText();
            String schemaType = parameters.get(4).getText();
            String included = parameters.get(5).getText();

            datasetColumns.add(new DatasetColumn(
                    name,
                    fieldName,
                    dataType,
                    renderType,
                    schemaType,
                    included
            ));
        }

        return datasetColumns;
    }

    private static String getDescription(WebDriver driver, int descriptionRowIndex) {
        WebElement element = driver.findElement(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr[" + descriptionRowIndex + "]/td[2]"));

        List<WebElement> linkElements = element.findElements(By.tagName("a"));
        if (linkElements.size() == 0) {
            return element.getText();
        }

        String linkText = linkElements.get(0).getAttribute("data-content");
        return linkText.substring(92, linkText.length() - 6);
    }

    private static void writeNameSection(String name, PrintWriter writer) {
        writer.println("# " + (name != null ? name : "No name"));
        writer.println();
    }

    private static void writeDatasetSection(DatasetMetadata datasetMetadata, PrintWriter writer) {
        writer.println("## Dataset");
        writer.println();
        writer.println("| Name | Value |");
        writer.println("| :--- | :---- |");

        if (datasetMetadata.catalogUrl != null) {
            writer.println(String.format("| Catalog | [Link](%1s) |", datasetMetadata.catalogUrl));
        }

        if (datasetMetadata.metadataUrl != null) {
            writer.println(String.format("| Metadata | [Link](%1s) |", datasetMetadata.metadataUrl));
        }

        if (datasetMetadata.dataJsonUrl != null) {
            writer.println(String.format("| Data: JSON | [100 Rows](%1s) |", datasetMetadata.dataJsonUrl));
        }

        if (datasetMetadata.dataCsvUrl != null) {
            writer.println(String.format("| Data: CSV | [100 Rows](%1s) |", datasetMetadata.dataCsvUrl));
        }

        if (datasetMetadata.host != null) {
            writer.println(String.format("| Host | %1s |", datasetMetadata.host));
        }

        if (datasetMetadata.id != null) {
            writer.println(String.format("| Id | %1s |", datasetMetadata.id));
        }

        if (datasetMetadata.name != null) {
            writer.println(String.format("| Name | %1s |", datasetMetadata.name));
        }

        if (datasetMetadata.attribution != null) {
            writer.println(String.format("| Attribution | %1s |", datasetMetadata.attribution));
        }

        if (datasetMetadata.category != null) {
            writer.println(String.format("| Category | %1s |", datasetMetadata.category));
        }

        if (datasetMetadata.tags != null) {
            writer.println(String.format("| Tags | %1s |", datasetMetadata.tags));
        }

        if (datasetMetadata.createdDate != null) {
            writer.println(String.format("| Created | %1s |", datasetMetadata.createdDate));
        }

        if (datasetMetadata.publicationDate != null) {
            writer.println(String.format("| Publication Date | %1s |", datasetMetadata.publicationDate));
        }

        if (datasetMetadata.rowsUpdatedDate != null) {
            writer.println(String.format("| Rows Updated | %1s |", datasetMetadata.rowsUpdatedDate));
        }

        writer.println();
    }

    private static void writeDescriptionSection(String description, PrintWriter writer) {
        if (StringUtils.isEmpty(description)) return;

        writer.println("## Description");
        writer.println();
        writer.println(description);
        writer.println();
    }

    private static void writeColumnsSection(List<DatasetColumn> columns, PrintWriter writer) {
        writer.println("## Columns");
        writer.println();
        writer.println("```ls");

        String nameColumnHeader = "Name";
        String fieldNameColumnHeader = "Field Name";
        String dataTypeColunmHeader = "Data Type";
        String renderTypeColumnHeader = "Render Type";
        String schemaTypeColumnHeader = "Schema Type";
        String includedColumnHeader = "Included";

        int nameColumnWidth = nameColumnHeader.length();
        int fieldNameColumnWidth = fieldNameColumnHeader.length();
        int dataTypeColumnWidth = dataTypeColunmHeader.length();
        int renderTypeColumnWidth = renderTypeColumnHeader.length();
        int schemaTypeColumnWidth = schemaTypeColumnHeader.length();
        int includedColumnWidth = includedColumnHeader.length();

        for (DatasetColumn column : columns) {
            nameColumnWidth = Math.max(nameColumnWidth, column.name.length());
            fieldNameColumnWidth = Math.max(fieldNameColumnWidth, column.fieldName.length());
            dataTypeColumnWidth = Math.max(dataTypeColumnWidth, column.dataType.length());
            renderTypeColumnWidth = Math.max(renderTypeColumnWidth, column.renderType.length());
            schemaTypeColumnWidth = Math.max(schemaTypeColumnWidth, column.schemaType.length());
            includedColumnWidth = Math.max(includedColumnWidth, column.included.length());
        }

        StringBuilder tableBuilder = new StringBuilder();

        String formattedIncludedColumnHeader = StringUtils.rightPad(includedColumnHeader, includedColumnWidth);
        tableBuilder.append("| " + formattedIncludedColumnHeader + " | ");

        String formattedSchemaTypeColumnHeader = StringUtils.rightPad(schemaTypeColumnHeader, schemaTypeColumnWidth);
        tableBuilder.append(formattedSchemaTypeColumnHeader + " | ");

        String formattedFieldNameColumnHeader = StringUtils.rightPad(fieldNameColumnHeader, fieldNameColumnWidth);
        tableBuilder.append(formattedFieldNameColumnHeader + " | ");

        String formattedNameColumnHeader = StringUtils.rightPad(nameColumnHeader, nameColumnWidth);
        tableBuilder.append(formattedNameColumnHeader + " | ");

        String formattedDataTypeColunmHeader = StringUtils.rightPad(dataTypeColunmHeader, dataTypeColumnWidth);
        tableBuilder.append(formattedDataTypeColunmHeader + " | ");

        String formattedRenderTypeColumnHeader = StringUtils.rightPad(renderTypeColumnHeader, renderTypeColumnWidth);
        tableBuilder.append(formattedRenderTypeColumnHeader + " |");

        tableBuilder.append("\n");

        tableBuilder.append("| " + StringUtils.repeat('=', includedColumnWidth) + " | ");
        tableBuilder.append(StringUtils.repeat('=', schemaTypeColumnWidth) + " | ");
        tableBuilder.append(StringUtils.repeat('=', fieldNameColumnWidth) + " | ");
        tableBuilder.append(StringUtils.repeat('=', nameColumnWidth) + " | ");
        tableBuilder.append(StringUtils.repeat('=', dataTypeColumnWidth) + " | ");
        tableBuilder.append(StringUtils.repeat('=', renderTypeColumnWidth) + " |");

        tableBuilder.append("\n");

        for (DatasetColumn column : columns) {

            String formattedIncludedColumnValue = StringUtils.rightPad(column.included, includedColumnWidth);
            tableBuilder.append("| " + formattedIncludedColumnValue + " | ");

            String formattedSchemaTypeColumnValue = StringUtils.rightPad(column.schemaType, schemaTypeColumnWidth);
            tableBuilder.append(formattedSchemaTypeColumnValue + " | ");

            String formattedFieldNameColumnValue = StringUtils.rightPad(column.fieldName, fieldNameColumnWidth);
            tableBuilder.append(formattedFieldNameColumnValue + " | ");

            String formattedNameColumnValue = StringUtils.rightPad(column.name, nameColumnWidth);
            tableBuilder.append(formattedNameColumnValue + " | ");

            String formattedDataTypeColumnValue = StringUtils.rightPad(column.dataType, dataTypeColumnWidth);
            tableBuilder.append(formattedDataTypeColumnValue + " | ");

            String formattedRenderTypeColumnValue = StringUtils.rightPad(column.renderType, renderTypeColumnWidth);
            tableBuilder.append(formattedRenderTypeColumnValue + " |");

            tableBuilder.append("\n");
        }

        writer.print(tableBuilder.toString());
        writer.println("```");
        writer.println();
    }

    private static void writeTimeFieldSection(String time, String format, PrintWriter writer) {
        writer.println("## Time Field");
        writer.println();
        writer.println("```ls");
        writer.println("Value = " + time);
        writer.println("Format & Zone = " + format);
        writer.println("```");
        writer.println();
    }

    private static void writeSeriesFieldSection(String prefix, String included, String excluded, String annotation, PrintWriter writer) {

        if (StringUtils.isEmpty(prefix) &&
                (StringUtils.isEmpty(included) || included.equals("*")) &&
                StringUtils.isEmpty(excluded) &&
                StringUtils.isEmpty(annotation)) return;

        writer.println("## Series Fields");
        writer.println();
        writer.println("```ls");

        if (!StringUtils.isEmpty(prefix)) {
            writer.println("Metric Prefix = " + prefix);
        }

        if (!StringUtils.isEmpty(included) && !included.equals("*")) {
            writer.println("Included Fields = " + included);
        }

        if (!StringUtils.isEmpty(excluded)) {
            writer.println("Excluded Fields = " + excluded);
        }

        if (!StringUtils.isEmpty(annotation)) {
            writer.println("Annotation Fields = " + annotation);
        }

        writer.println("```");
        writer.println();
    }

    private static void writeDataCommandsSection(List<String> commands, PrintWriter writer) {
        writer.println("## Data Commands");
        writer.println();
        writer.println("```ls");

        for (int i = 0; i < commands.size(); i++) {
            writer.println(commands.get(i) == null ? "" : commands.get(i));

            if (i != commands.size() - 1) {
                writer.println();
            }
        }

        writer.println("```");
        writer.println();
    }

    private static void writeMetadataCommandsSection(List<String> metacommands, PrintWriter writer) {
        writer.println("## Meta Commands");
        writer.println();
        writer.println("```ls");

        for (int i = 0; i < metacommands.size(); i++) {
            writer.println(metacommands.get(i) == null ? "" : metacommands.get(i));

            if (i != metacommands.size() - 1) {
                writer.println();
            }
        }

        writer.print("```");
    }

    private static class DatasetSummaryInfo {

        final String host;

        final String name;

        final String descriptionFilePath;

        final String category;

        final String rowsUpdatedTime;

        private DatasetSummaryInfo(
                String host,
                String name,
                String descriptionFilePath,
                String category,
                String rowsUpdatedTime) {
            this.host = host;
            this.name = name;
            this.descriptionFilePath = descriptionFilePath;
            this.category = category;
            this.rowsUpdatedTime = rowsUpdatedTime;
        }
    }

    private static class DatasetMetadata {

        final String catalogUrl;
        final String metadataUrl;
        final String dataJsonUrl;
        final String dataCsvUrl;
        final String host;
        final String id;
        final String name;
        final String description;
        final String tags;
        final String attribution;
        final String category;
        final String createdDate;
        final String publicationDate;
        final String rowsUpdatedDate;

        private DatasetMetadata(
                String catalogUrl,
                String metadataUrl,
                String dataJsonUrl,
                String dataCsvUrl,
                String host,
                String id,
                String name,
                String description,
                String tags, String attribution,
                String category,
                String createdDate,
                String publicationDate,
                String rowsUpdatedDate) {
            this.catalogUrl = catalogUrl;
            this.metadataUrl = metadataUrl;
            this.dataJsonUrl = dataJsonUrl;
            this.dataCsvUrl = dataCsvUrl;
            this.host = host;
            this.id = id;
            this.name = name;
            this.description = description;
            this.tags = tags;
            this.attribution = attribution;
            this.category = category;
            this.createdDate = createdDate;
            this.publicationDate = publicationDate;
            this.rowsUpdatedDate = rowsUpdatedDate;
        }
    }

    private static class DatasetColumn {

        final String name;
        final String fieldName;
        final String dataType;
        final String renderType;
        final String schemaType;
        final String included;

        private DatasetColumn(
                String name,
                String fieldName,
                String dataType,
                String renderType,
                String schemaType,
                String included) {
            this.name = name;
            this.fieldName = fieldName;
            this.dataType = dataType;
            this.renderType = renderType;
            this.schemaType = schemaType;
            this.included = included;
        }
    }
}
