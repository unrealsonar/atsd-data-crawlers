package com.axibase.selenium;

/*
 * Created by boriss on 07.02.17.
 * parse some datasets from catalog.data.gov with some info via ATSD Collector
 */


import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
//import org.openqa.selenium.chrome.ChromeDriver;

import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
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
    private static final int columnsNumber = 6;

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

/*
        System.setProperty("webdriver.chrome.driver", "chromedriver");
        WebDriver driver = new ChromeDriver();
*/

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
        //driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

        ExpectedCondition<Boolean> waitCondition = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
                    }
                };
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
                DatasetSummaryInfo info = processUrl(urlPropertyName, jobName, driver, waitCondition, wait, pr);

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
                wait.until(waitCondition);
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
                writer.println("Name | Category | Rows Updated");
                writer.println("---- | -------- | ------------");

                for (DatasetSummaryInfo datasetInfo : infoByHost.getValue()) {
                    writer.println(
                            String.format("[%1s](%2s) | %3s | %4s",
                                    datasetInfo.name,
                                    datasetInfo.descriptionFilePath,
                                    datasetInfo.category,
                                    datasetInfo.rowsUpdatedTime));
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
            ExpectedCondition<Boolean> waitCondition,
            WebDriverWait wait,
            Properties pr) throws IOException, URISyntaxException {

        log("Adding new Socrata job...");

        WebElement link = driver.findElement(By.linkText("Jobs"));
        link.click();

        link = driver.findElement(By.linkText(jobName));
        link.click();

        link = driver.findElement(By.linkText("Create from URL"));
        link.click();
        wait.until(waitCondition);
        WebElement field = driver.findElement(By.id("inputWizardUrl"));
        field.sendKeys(urlPropertyName);

        wait.until(waitCondition);
        link = driver.findElement(By.id("btnWizardCreate"));
        link.click();

        wait.until(waitCondition);
        WebElement nameField = driver.findElement(By.id("name"));
        String check = nameField.getAttribute("value");

        if (check.equals("")) return null;

        field = driver.findElement(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr[2]/td[2]"));
        String name = field.getText();

        log("Socrata job for entity " + name + " is added");

        log("parsing dataset...");

        //[dataset]
        int l, j;
        int datasetlen = driver.findElements(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr")).size();

        String documentName = null;
        String category = null;
        String rowsUpdatedTime = null;

        StringBuilder dataBuilder = new StringBuilder();
        String desc = "";
        WebElement temp;
        //contents
        for (l = 2; l <= datasetlen; l++) {
            temp = driver.findElement(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr[" + l + "]/td[1]"));

            String element = temp.getText();

            if (element.equals("Description")) {
                temp = driver.findElement(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr[" + l + "]/td[2]/a"));
                desc = temp.getAttribute("data-content").substring(92, temp.getAttribute("data-content").length() - 6);
                continue;
            }

            temp = driver.findElement(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr[" + l + "]/td[2]"));
            String text = temp.getText();

            if (element.equals("Name")) {
                documentName = text;
            }

            if (element.equals("Category")) {
                category = text;
            }

            if (element.equals("Rows Updated")) {
                rowsUpdatedTime = text;
            }

            if (element.equals("Attribution Link")) {
                dataBuilder.append(String.format("* [Attribution Link](%1s)\n", text));
                continue;
            }

            dataBuilder.append(String.format("* %1s = %2s\n", element, text));
        }

        String data = dataBuilder.toString();

        log("parsing columns...");

        //[columns]
        int rowsNumber = driver.findElements(By.xpath("//*[@id=\"tblColumnInfos\"]/tbody/tr")).size();
        String[][] cols = new String[rowsNumber][columnsNumber];
        //table head
        for (l = 0; l < columnsNumber; l++) {
            temp = driver.findElement(By.xpath("//*[@id=\"tblColumnInfos\"]/tbody/tr[2]/th[" + (l + 1) + "]"));
            cols[0][l] = temp.getText();
        }
        //contents
        for (j = 1; j < rowsNumber - 1; j++) {
            for (l = 0; l < columnsNumber; l++) {
                temp = driver.findElement(By.xpath(
                        "//*[@id=\"tblColumnInfos\"]/tbody/tr[" + (j + 2) + "]/td[" + (l + 1) + "]"));
                cols[j][l] = temp.getText();
            }
        }

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
        int count = 0;
        String[] commands = new String[commandsNumber];
        String[] metacommands = new String[metacommandsNumber];

        temp = driver.findElement(By.xpath("//*[@id=\"testRow_2\"]/td/table/tbody/tr[3]/td[2]"));

        String[] strings = temp.getText().split("\n");

        for (j = 0; j < commandsNumber; j++) {
            if (strings[j].substring(0, 6).equals("series")) {
                commands[count++] = strings[j];
            }
        }

        count = 0;
        for (j = strings.length - 1; j >= 0; j--) {
            if (strings[j].startsWith("metric") ||
                    strings[j].startsWith("entity") ||
                    strings[j].startsWith("property")) {
                metacommands[count++] = strings[j];
            }
        }
        int mcN = count;

        log("writing in file...");

        //write in file
        File file = new File("reports/datasets/" + name + ".md");
        int[] offset = new int[columnsNumber];
        int max;

        if (!file.exists()) {
            file.createNewFile();
        }
        try (PrintWriter out = new PrintWriter(file.getAbsoluteFile())) {
            out.println("# " + (documentName != null ? documentName : "No name"));
            out.println();

            out.println("## Dataset");
            out.println();

            out.println("* [Dataset URL](" + urlPropertyName + "?max_rows=100" + ")");
            out.println("* [Catalog URL](" + pr.getProperty(urlPropertyName) + ")");

            int metadataLinkLength = urlPropertyName.indexOf("/rows");
            String metadataLink = "";
            if (metadataLinkLength > 0) {
                metadataLink = urlPropertyName.substring(0, metadataLinkLength);
            }
            out.println("* [Metadata URL](" + metadataLink + ")");

            out.println(data);

            out.println("## Description");
            out.println();
            out.println(desc);
            out.println();

            out.println("## Columns");
            out.println();
            out.println("```ls");

            for (int i1 = 0; i1 < columnsNumber; i1++) {
                max = 0;
                for (int i2 = 0; i2 < rowsNumber - 1; i2++) {
                    if (cols[i2][i1].length() > max) {
                        max = cols[i2][i1].length();
                    }
                }
                offset[i1] = max;
            }
            for (int i1 = 0; i1 < rowsNumber - 1; i1++) {
                if (i1 == 1) {
                    out.print("| ");
                    for (int i2 = 0; i2 < columnsNumber; i2++) {
                        for (int i3 = 0; i3 < offset[i2]; i3++) {
                            out.print("=");
                        }
                        out.print(" | ");
                    }
                    out.println();
                }
                out.print("| ");
                for (int i2 = 0; i2 < columnsNumber; i2++) {
                    out.printf("%-" + offset[i2] + "s | ", cols[i1][i2]);
                }
                out.println();
            }
            out.println("```");
            out.println();

            out.println("## Time Field");
            out.println();
            out.println("```ls");
            out.println("Value = " + time);
            out.println("Format & Zone = " + format);
            out.println("```");
            out.println();

            out.println("## Series Fields");
            out.println();
            out.println("```ls");
            out.println("Metric Prefix = " + prefix);
            out.println("Included Fields = " + included);
            out.println("Excluded Fields = " + excluded);
            out.println("Annotation Fields = " + annotation);
            out.println("```");
            out.println();

            out.println("## Data Commands");
            out.println();
            out.println("```ls");

            for (int k = 0; k < commands.length; k++) {
                out.println(commands[k] == null ? "" : commands[k]);

                if (k != commands.length - 1) {
                    out.println();
                }
            }

            out.println("```");
            out.println();

            out.println("## Meta Commands");
            out.println();
            out.println("```ls");

            for (int k = mcN - 1; k >= 0; k--) {
                out.println(metacommands[k] == null ? "" : metacommands[k]);

                if (k != 0) {
                    out.println();
                }
            }

            out.print("```");

        }

        URI datasetUri = new URI(urlPropertyName);
        String datasetHost = datasetUri.getHost();

        return new DatasetSummaryInfo(
                datasetHost,
                documentName,
                "datasets/" + name + ".md",
                category,
                rowsUpdatedTime);
    }

    private static class DatasetSummaryInfo {

        public final String host;

        public final String name;

        public final String descriptionFilePath;

        public final String category;

        public final String rowsUpdatedTime;

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
}
