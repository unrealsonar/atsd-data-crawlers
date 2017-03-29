package com.axibase.selenium;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Crawler {

    private static final int commandsNumber = 3;
    private static final int metacommandsNumber = 100;

    private WebDriver driver = null;
    private WebDriverWait wait = null;
    private String jobName = null;
    private Properties urlProperties = null;
    private String collectorMainPageUrl = null;

    public void init(
            String collector,
            String username,
            String password,
            String jobName,
            Properties urlProperties) {

        this.urlProperties = urlProperties;
        this.jobName = jobName;

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "phantomjs");
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
                new String[]{"--web-security=no", "--ignore-ssl-errors=yes", "--webdriver-loglevel=NONE"});

        driver = new PhantomJSDriver(caps);
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 1);

        driver.get(collector);
        WebElement field = driver.findElement(By.id("username"));
        field.sendKeys(username);
        field = driver.findElement(By.name("password"));
        field.sendKeys(password);
        field.submit();

        collectorMainPageUrl = driver.getCurrentUrl();
    }

    public void close() {
        driver.close();
    }

    public boolean process(String url) {

        try {

            boolean success = processUrl(url);

            if (!success) {
                driver.get(collectorMainPageUrl);
                return false;
            }

            return true;

        } catch (Exception ex) {
            driver.get(collectorMainPageUrl);
            return false;
        }
    }

    private boolean processUrl(String urlPropertyName) throws IOException, URISyntaxException {

        if (driver == null || wait == null || urlProperties == null) return false;

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

        if (check.equals("")) return false;

        String pageSource = driver.getPageSource();
        DatasetMetadata datasetMetadata = getDatasetMetadata(driver, urlPropertyName, urlProperties);
        List<DatasetColumn> datasetColumns = getDatasetColumns(driver);
        DatasetTime datasetTime = getDatasetTime(driver);
        DatasetSeriesField datasetSeriesField = getDatasetSeriesField(driver);
        DatasetCommands commands = getDatasetCommands(driver);
        DatasetTopRecordsTable datasetTopRecordsTable = getDatasetTopRecordsTable(driver);

        writeDatasetFile(
                datasetMetadata,
                datasetColumns,
                datasetTime,
                datasetSeriesField,
                commands,
                datasetTopRecordsTable);

        writePageSource(datasetMetadata.id, pageSource);

        return true;
    }

    //region Parsing

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

        }

        int datasetlen = driver.findElements(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr")).size();

        String name = null;
        String category = null;
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
                case "Tags": {
                    tags = itemValue.replaceAll("[\\[\\]]", "");
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
                publicationDate
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

    private static DatasetCommands getDatasetCommands(WebDriver driver) {

        ArrayList<String> commands = new ArrayList<>(commandsNumber);
        ArrayList<String> metacommands = new ArrayList<>(metacommandsNumber);

        WebElement element = driver.findElement(By.xpath("//*[@id=\"testRow_2\"]/td/table/tbody/tr[3]/td[2]"));
        String[] lines = element.getText().split("\n");

        List<String> singleLineCommands = new ArrayList<>(lines.length);

        StringBuilder commandBuilder = null;
        for (String line : lines) {

            if (line == null) continue;

            int quotesCount = StringUtils.countMatches(line, "\"");

            // is line is a part of multiline command
            if (commandBuilder != null) {

                // end of command
                if (quotesCount % 2 != 0) {
                    commandBuilder.append(line);
                    singleLineCommands.add(commandBuilder.toString());
                    commandBuilder = null;
                    continue;
                }

                commandBuilder.append(line);
                commandBuilder.append("\n");
            }

            // start of multiline command
            if (commandBuilder == null && quotesCount % 2 != 0) {
                commandBuilder = new StringBuilder();
                commandBuilder.append(line);
                commandBuilder.append("\n");
                continue;
            }

            singleLineCommands.add(line);
        }

        for (String line : singleLineCommands) {

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

        return new DatasetCommands(metacommands, commands);
    }

    private static DatasetTime getDatasetTime(WebDriver driver) {

        WebElement field = driver.findElement(By.id("timeField_0"));
        String time = field.getAttribute("value");

        if (StringUtils.isEmpty(time)) {
            field = driver.findElement(By.id("timeDefault_0"));
            time = field.getAttribute("value");
        }

        field = driver.findElement(By.id("timeFormat_0"));
        String format = field.getAttribute("value");

        return new DatasetTime(time, format);
    }

    private static DatasetSeriesField getDatasetSeriesField(WebDriver driver) {

        WebElement field = driver.findElement(By.id("metricPrefix_0"));
        String prefix = field.getAttribute("value");

        field = driver.findElement(By.id("includedFields_0"));
        String included = field.getAttribute("value");

        field = driver.findElement(By.id("excludedFields_0"));
        String excluded = field.getAttribute("value");

        field = driver.findElement(By.id("annotationFields_0"));
        String annotation = field.getAttribute("value");

        return new DatasetSeriesField(prefix, included, excluded, annotation);
    }

    private static DatasetTopRecordsTable getDatasetTopRecordsTable(WebDriver driver) {
        WebElement tableElement = driver.findElement(By.xpath("//*[@id=\"testRow_2\"]/td/table/tbody/tr[5]/td[2]"));
        if (tableElement == null) return null;

        List<WebElement> rowsElements = tableElement.findElements(By.tagName("tr"));
        if (rowsElements == null || rowsElements.size() == 0) return null;

        WebElement headersContainer = rowsElements.get(0);
        if (headersContainer == null) return null;

        List<WebElement> headersElements = headersContainer.findElements(By.tagName("th"));
        if (headersElements == null || headersElements.size() == 0) return null;

        List<String> headers = new ArrayList<>(headersElements.size());
        for (WebElement element : headersElements) {
            if (element == null) {
                headers.add("null");
                continue;
            }

            headers.add(element.getText());
        }

        if (rowsElements.size() == 1) {
            return new DatasetTopRecordsTable(headers, new ArrayList<List<String>>());
        }

        ArrayList<List<String>> rows = new ArrayList<>(rowsElements.size() - 1);
        for (int i = 1; i < rowsElements.size(); i++) {
            WebElement rowElement = rowsElements.get(i);
            if (rowElement == null) continue;

            List<WebElement> rowElements = rowElement.findElements(By.tagName("td"));
            if (rowElements == null || rowElements.size() == 0) continue;

            ArrayList<String> rowValues = new ArrayList<>(rowElements.size());
            for (WebElement rowValueElement : rowElements) {
                if (rowValueElement == null) {
                    rowValues.add("null");
                    continue;
                }

                rowValues.add(rowValueElement.getText());
            }

            rows.add(rowValues);
        }

        return new DatasetTopRecordsTable(headers, rows);
    }

    //endregion

    //region Writing

    private static void writeDatasetFile(
            DatasetMetadata datasetMetadata,
            List<DatasetColumn> datasetColumns,
            DatasetTime datasetTime,
            DatasetSeriesField datasetSeriesField,
            DatasetCommands commands,
            DatasetTopRecordsTable datasetTopRecordsTable) throws IOException {
        File file = new File("reports/datasets/" + datasetMetadata.id + ".md");

        if (!file.exists()) {
            File parentFile = file.getParentFile();
            parentFile.mkdirs();

            file.createNewFile();
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            writeNameSection(datasetMetadata.name, writer);
            writeDatasetSection(datasetMetadata, writer);
            writeDescriptionSection(datasetMetadata.description, writer);
            writeColumnsSection(datasetColumns, writer);
            writeTimeFieldSection(datasetTime, writer);
            writeSeriesFieldSection(datasetSeriesField, writer);
            writeDataCommandsSection(commands.commands, writer);
            writeMetadataCommandsSection(commands.metacommands, writer);
            writeTopRecordsSection(datasetTopRecordsTable, writer);
        }
    }

    private static void writePageSource(String datasetId, String pageSource) throws IOException {
        File file = new File("reports/webpages/" + datasetId + ".htm");

        if (!file.exists()) {
            File parentFile = file.getParentFile();
            parentFile.mkdirs();

            file.createNewFile();
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.write(pageSource);
        }
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

    private static void writeTimeFieldSection(DatasetTime datasetTime, PrintWriter writer) {
        writer.println("## Time Field");
        writer.println();
        writer.println("```ls");
        writer.println("Value = " + datasetTime.time);
        writer.println("Format & Zone = " + datasetTime.format);
        writer.println("```");
        writer.println();
    }

    private static void writeSeriesFieldSection(DatasetSeriesField datasetSeriesField, PrintWriter writer) {

        String prefix = datasetSeriesField.prefix;
        String included = datasetSeriesField.included;
        String excluded = datasetSeriesField.excluded;
        String annotation = datasetSeriesField.annotation;

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

            // TODO removed date, delete this in the release version
            String metacommand = metacommands.get(i);
            if (metacommand == null) continue;

            if (metacommand.startsWith("property")) {

                int dateFieldIndex = metacommand.indexOf(" d:");

                if (dateFieldIndex >= 0) {
                    metacommand = metacommand.substring(0, dateFieldIndex) + metacommand.substring(dateFieldIndex + " d:2017-01-01T00:00:00.000Z".length());
                }
            }

            writer.println(metacommand);

            if (i != metacommands.size() - 1) {
                writer.println();
            }
        }

        writer.println("```");
        writer.println();
    }

    private static void writeTopRecordsSection(DatasetTopRecordsTable datasetTopRecordsTable, PrintWriter writer) {
        writer.println("## Top Records");
        writer.println();
        writer.println("```ls");

        if (datasetTopRecordsTable == null) {
            writer.print("```");
            return;
        }

        List<Integer> columnsMaxWidth = new ArrayList<>(datasetTopRecordsTable.headers.size());
        for (String header : datasetTopRecordsTable.headers) {
            columnsMaxWidth.add(header.length());
        }

        for (List<String> row : datasetTopRecordsTable.rows) {
            for (int i = 0; i < row.size(); i++) {
                String rowValue = row.get(i);
                int maxColumnWidth = columnsMaxWidth.get(i);
                if (rowValue.length() > maxColumnWidth) {
                    columnsMaxWidth.set(i, rowValue.length());
                }
            }
        }

        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("| ");
        List<String> headers = datasetTopRecordsTable.headers;
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            int columnWidth = columnsMaxWidth.get(i);
            headerBuilder.append(StringUtils.rightPad(header, columnWidth));
            headerBuilder.append(" | ");
        }
        headerBuilder.append("\n");

        headerBuilder.append("| ");
        for (int i = 0; i < datasetTopRecordsTable.headers.size(); i++) {
            int columnWidth = columnsMaxWidth.get(i);
            headerBuilder.append(StringUtils.repeat('=', columnWidth));
            headerBuilder.append(" | ");
        }
        writer.println(headerBuilder.toString());

        for (List<String> row : datasetTopRecordsTable.rows) {
            StringBuilder rowBuilder = new StringBuilder();
            rowBuilder.append("| ");

            for (int i = 0; i < row.size(); i++) {
                String rowValue = row.get(i);
                int columnWidth = columnsMaxWidth.get(i);
                rowBuilder.append(StringUtils.rightPad(rowValue, columnWidth));
                rowBuilder.append(" | ");
            }

            writer.println(rowBuilder.toString());
        }

        writer.print("```");
    }

    //endregion

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
                String publicationDate) {
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

    private static class DatasetCommands {
        public final List<String> metacommands;
        public final List<String> commands;

        private DatasetCommands(List<String> metacommands, List<String> commands) {
            this.metacommands = metacommands;
            this.commands = commands;
        }
    }

    private static class DatasetTime {
        public final String time;
        public final String format;

        private DatasetTime(String time, String format) {
            this.time = time;
            this.format = format;
        }
    }

    private static class DatasetSeriesField {
        public final String prefix;
        public final String included;
        public final String excluded;
        public final String annotation;

        private DatasetSeriesField(String prefix, String included, String excluded, String annotation) {
            this.prefix = prefix;
            this.included = included;
            this.excluded = excluded;
            this.annotation = annotation;
        }
    }

    private static class DatasetTopRecordsTable {
        public final List<String> headers;
        public final List<List<String>> rows;

        private DatasetTopRecordsTable(List<String> headers, List<List<String>> rows) {
            this.headers = headers;
            this.rows = rows;
        }
    }
}
