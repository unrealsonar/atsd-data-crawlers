package com.axibase.irscrawler;

import com.axibase.irscrawler.common.HtmlPageLoader;
import com.axibase.irscrawler.common.Result;
import com.axibase.irscrawler.common.Series;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Crawler {
    private final HtmlPageLoader htmlLoader = new HtmlPageLoader();

    public Result<Boolean> process(Date startDate) {
        try {
            Result<List<StatisticsUrl>> statisticsUrsResult = getStatisticsUrls();
            if (statisticsUrsResult.errorText != null) {
                return new Result<>(statisticsUrsResult.errorText, false);
            }

            List<StatisticsUrl> filteredStatisticsUrls = new ArrayList<>(statisticsUrsResult.result.size());
            for (StatisticsUrl url : statisticsUrsResult.result) {
                if (url.date.before(startDate)) continue;

                filteredStatisticsUrls.add(url);
            }

            List<Series> seriesList = processStatisticsUrls(filteredStatisticsUrls);

            Result<Boolean> writeResult = writeSeriesFile(seriesList);
            if (writeResult.errorText != null) {
                return new Result<>(writeResult.errorText, false);
            }

            return new Result<>(null, true);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result<>(ex.getMessage(), false);
        }
    }


    public Result<Boolean> process() {
        try {
            Result<List<StatisticsUrl>> statisticsUrsResult = getStatisticsUrls();
            if (statisticsUrsResult.errorText != null) {
                return new Result<>(statisticsUrsResult.errorText, false);
            }

            List<Series> seriesList = processStatisticsUrls(statisticsUrsResult.result);

            Result<Boolean> writeResult = writeSeriesFile(seriesList);
            if (writeResult.errorText != null) {
                return new Result<>(writeResult.errorText, false);
            }

            return new Result<>(null, true);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result<>(ex.getMessage(), false);
        }
    }

    private List<Series> processStatisticsUrls(List<StatisticsUrl> statisticsUrls) {
        List<Series> seriesList = new ArrayList<>();
        int currentUrl = 0;
        for (StatisticsUrl statisticsUrl : statisticsUrls) {
            currentUrl++;
            //if (currentUrl < 88) continue;
            Result<String> statisticsHtmlResult = htmlLoader.loadHtml(statisticsUrl.url);
            if (statisticsHtmlResult.errorText != null) {
                System.out.println(statisticsHtmlResult.errorText);
                continue;
            }

            Result<List<Series>> seriesResult = getSeriesCommands(statisticsHtmlResult.result, statisticsUrl.date);
            if (seriesResult.errorText != null) {
                System.out.println(String.format("Error processing url %s : %s", statisticsUrl.url, seriesResult.errorText));
                continue;
            }

            seriesList.addAll(seriesResult.result);
            System.out.println(String.format("Urls: %s/%s", currentUrl, statisticsUrls.size()));
        }

        return seriesList;
    }

    private Result<Boolean> writeSeriesFile(List<Series> seriesList) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            try (PrintWriter writer = new PrintWriter("series.txt")) {
                for (Series series : seriesList) {
                    String date = simpleDateFormat.format(series.date);
                    writer.println(
                            String.format(
                                    "series d:%s e:%s t:section=\"%s\" t:type=\"%s\" m:irs_season.count_year_current=%s m:irs_season.count_year_previous=%s",
                                    date,
                                    series.entity,
                                    series.section,
                                    series.type,
                                    series.currentYearValue,
                                    series.previousYearValue));
                }
            }
        } catch (IOException ex) {
            return new Result<>(ex.getMessage(), false);
        }

        return new Result<>(null, true);
    }

    private Result<List<StatisticsUrl>> getStatisticsUrls() {

        Result<String> mainPageHtmlResult;
        try {
            mainPageHtmlResult = htmlLoader.loadHtml(new URL("https://www.irs.gov/uac/2017-and-prior-year-filing-season-statistics"));
            if (mainPageHtmlResult.errorText != null) {
                return new Result<>(mainPageHtmlResult.errorText, null);
            }
        } catch (MalformedURLException ex) {
            return new Result<>(ex.getMessage(), null);
        }

        Document htmlDocument = Jsoup.parse(mainPageHtmlResult.result);
        if (htmlDocument == null) return new Result<>("Html parse error", null);

        Element tablesContainer = htmlDocument.select("div.wysiwyg").first();
        if (tablesContainer == null) return new Result<>("Html parse error", null);

        Elements tablesElements = htmlDocument.select("table");
        if (tablesElements == null) return new Result<>("Html parse error", null);

        List<StatisticsUrl> urls = new ArrayList<>();
        for (Element tableElement : tablesElements) {
            if (tableElement == null) continue;

            Elements urlElements = tableElement.select("a");
            if (urlElements == null) continue;

            for (Element urlElement : urlElements) {
                if (urlElement == null) continue;

                String url = urlElement.attr("href");
                if (url == null) continue;

                String dateString = urlElement.text();
                DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
                Date date;
                try {
                    date = format.parse(dateString);
                } catch (ParseException ex) {
                    return new Result<>("Html parse error", null);
                }

                if (!url.startsWith("https://")) {
                    url = "https://www.irs.gov" + url;
                }

                URL resultUrl;
                try {
                    resultUrl = new URL(url);
                } catch (MalformedURLException e) {
                    continue;
                }

                urls.add(new StatisticsUrl(resultUrl, date));
            }
        }

        return new Result<>(null, urls);
    }

    private static Result<List<Series>> getSeriesCommands(String statisticsHtml, Date date) {
        Document htmlDocument = Jsoup.parse(statisticsHtml);
        if (htmlDocument == null) return new Result<>("Html parse error", null);

        Element header = htmlDocument.select("h1").first();
        if (header == null) return new Result<>("Html parse error", null);

        Element tableElement = htmlDocument.select("table").first();
        if (tableElement == null) return new Result<>("Html parse error", null);

        Elements rowsElements = tableElement.select("tr");
        if (rowsElements == null) return new Result<>("Html parse error", null);

        List<Series> seriesList = new ArrayList<>();

        Map<Categories, ArrayList<Element>> filteredRowElements = new HashMap<>();
        ArrayList<Element> curentElementSet = null;
        for (Element rowElement : rowsElements) {
            Elements columns = rowElement.select("td");

            Element firstColumnElement = columns.get(0);
            if (firstColumnElement == null) continue;

            String rawText = firstColumnElement.html();

            if (rawText.contains("Individual Income")) {
                curentElementSet = new ArrayList<>();
                filteredRowElements.put(Categories.Individual_Income_Tax_Returns, curentElementSet);
                continue;
            }

            if (rawText.contains("E-filing Receipts")) {
                curentElementSet = new ArrayList<>();
                filteredRowElements.put(Categories.E_filing_Receipts, curentElementSet);
                continue;
            }

            if (rawText.contains("Web Usage")) {
                curentElementSet = new ArrayList<>();
                filteredRowElements.put(Categories.Web_Usage, curentElementSet);
                continue;
            }

            if (rawText.contains("Total Refunds")) {
                curentElementSet = new ArrayList<>();
                filteredRowElements.put(Categories.Total_Refunds, curentElementSet);
                continue;
            }

            if (rawText.contains("Direct Deposit Refunds")) {
                curentElementSet = new ArrayList<>();
                filteredRowElements.put(Categories.Direct_Deposit_Refunds, curentElementSet);
                continue;
            }

            Result<String> text = getTextValueFromCell(firstColumnElement);
            if (text.result.equals("&nbsp;")) continue;

            if (curentElementSet != null) {
                curentElementSet.add(rowElement);
            }
        }

        List<Element> elements = filteredRowElements.get(Categories.Individual_Income_Tax_Returns);
        if (elements != null) {
            Elements totalReceiptsRow = elements.get(0).select("td");
            Integer previousYearValue = getNumericValueFromCell(totalReceiptsRow.get(1));
            Integer currentYearValue = getNumericValueFromCell(totalReceiptsRow.get(2));

            seriesList.add(new Series(
                    date,
                    "irs.gov",
                    "Individual Income Tax Returns",
                    "Total Returns Received",
                    currentYearValue,
                    previousYearValue));

            Elements totalProcessedRow = elements.get(1).select("td");
            previousYearValue = getNumericValueFromCell(totalProcessedRow.get(1));
            currentYearValue = getNumericValueFromCell(totalProcessedRow.get(2));

            seriesList.add(new Series(
                    date,
                    "irs.gov",
                    "Individual Income Tax Returns",
                    "Total Returns Processed",
                    currentYearValue,
                    previousYearValue));
        }

        elements = filteredRowElements.get(Categories.E_filing_Receipts);
        if (elements != null) {
            Elements totalReceiptsRow = elements.get(0).select("td");
            Integer previousYearValue = getNumericValueFromCell(totalReceiptsRow.get(1));
            Integer currentYearValue = getNumericValueFromCell(totalReceiptsRow.get(2));

            seriesList.add(new Series(
                    date,
                    "irs.gov",
                    "E-filing Receipts",
                    "Total",
                    currentYearValue,
                    previousYearValue));

            Elements taxProfessionalRow = elements.get(1).select("td");
            previousYearValue = getNumericValueFromCell(taxProfessionalRow.get(1));
            currentYearValue = getNumericValueFromCell(taxProfessionalRow.get(2));

            seriesList.add(new Series(
                    date,
                    "irs.gov",
                    "E-filing Receipts",
                    "Tax Professionals",
                    currentYearValue,
                    previousYearValue));

            Elements selfPreparedRow = elements.get(2).select("td");
            previousYearValue = getNumericValueFromCell(selfPreparedRow.get(1));
            currentYearValue = getNumericValueFromCell(selfPreparedRow.get(2));

            seriesList.add(new Series(
                    date,
                    "irs.gov",
                    "E-filing Receipts",
                    "Self-prepared",
                    currentYearValue,
                    previousYearValue));
        }

        elements = filteredRowElements.get(Categories.Web_Usage);
        if (elements != null) {
            Elements totalVisits = elements.get(0).select("td");
            Integer previousYearValue = getNumericValueFromCell(totalVisits.get(1));
            Integer currentYearValue = getNumericValueFromCell(totalVisits.get(2));

            seriesList.add(new Series(
                    date,
                    "irs.gov",
                    "Web Usage",
                    "Visits to IRS.gov",
                    currentYearValue,
                    previousYearValue));
        }

        elements = filteredRowElements.get(Categories.Total_Refunds);
        if (elements != null) {
            Elements numberRow = elements.get(0).select("td");
            Integer previousYearValue = getNumericValueFromCell(numberRow.get(1));
            Integer currentYearValue = getNumericValueFromCell(numberRow.get(2));

            seriesList.add(new Series(
                    date,
                    "irs.gov",
                    "Total Refunds",
                    "Number",
                    currentYearValue,
                    previousYearValue));

            Elements amountRow = elements.get(1).select("td");
            previousYearValue = getNumericValueFromCell(amountRow.get(1));
            currentYearValue = getNumericValueFromCell(amountRow.get(3));

            seriesList.add(new Series(
                    date,
                    "irs.gov",
                    "Total Refunds",
                    "Amount",
                    currentYearValue,
                    previousYearValue));

            Elements selfPreparedRow = elements.get(2).select("td");
            previousYearValue = getNumericValueFromCell(selfPreparedRow.get(1));
            currentYearValue = getNumericValueFromCell(selfPreparedRow.get(2));

            seriesList.add(new Series(
                    date,
                    "irs.gov",
                    "Total Refunds",
                    "Average refund",
                    currentYearValue,
                    previousYearValue));
        }

        elements = filteredRowElements.get(Categories.Direct_Deposit_Refunds);
        if (elements != null) {
            Elements numberRow = elements.get(0).select("td");
            Integer previousYearValue = getNumericValueFromCell(numberRow.get(1));
            Integer currentYearValue = getNumericValueFromCell(numberRow.get(2));

            seriesList.add(new Series(
                    date,
                    "irs.gov",
                    "Direct Deposit Refunds",
                    "Number",
                    currentYearValue,
                    previousYearValue));

            Elements amountRow = elements.get(1).select("td");
            previousYearValue = getNumericValueFromCell(amountRow.get(1));
            currentYearValue = getNumericValueFromCell(amountRow.get(3));

            seriesList.add(new Series(
                    date,
                    "irs.gov",
                    "Direct Deposit Refunds",
                    "Amount",
                    currentYearValue,
                    previousYearValue));

            if (elements.size() > 2) {
                Elements selfPreparedRow = elements.get(2).select("td");
                previousYearValue = getNumericValueFromCell(selfPreparedRow.get(1));
                currentYearValue = getNumericValueFromCell(selfPreparedRow.get(2));

                seriesList.add(new Series(
                        date,
                        "irs.gov",
                        "Direct Deposit Refunds",
                        "Average refund",
                        currentYearValue,
                        previousYearValue));
            }
        }

        return new Result<>(null, seriesList);
    }

    private static Result<String> getTextValueFromCell(Element cellElement) {
        if (cellElement.children().size() == 0) {
            return new Result<>(null, cellElement.html());
        }

        Element cellValue = cellElement.children().first();
        if (cellValue == null) return new Result<>("Html parse error", null);

        if (cellValue.children().size() != 0) {
            cellValue = cellValue.children().first();
        }

        if (cellValue.children().size() != 0) {
            cellValue = cellValue.children().first();
        }

        return new Result<>(null, cellValue.html());
    }

    private static Integer getNumericValueFromCell(Element cellElement) {
        String text;
        if (cellElement.children().size() == 2) {
            text = cellElement.child(1).text() + cellElement.text();
            return parseNumericValue(text);
        }

        if (cellElement.children().size() == 0) {
            text = cellElement.html();
            return parseNumericValue(text);
        }

        cellElement = cellElement.children().first();
        return parseNumericValue(cellElement.text());
    }

    private static Integer parseNumericValue(String cellValue) {
        String text = cellValue.replace(".", "").replaceAll("[^\\d.]", "");
        Integer result;
        try {
            result = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return null;
        }

        return result;
    }

    private static class StatisticsUrl {
        public final URL url;
        public final Date date;

        private StatisticsUrl(URL url, Date date) {
            this.url = url;
            this.date = date;
        }
    }

    private enum Categories {
        Individual_Income_Tax_Returns,
        E_filing_Receipts,
        Web_Usage,
        Total_Refunds,
        Direct_Deposit_Refunds
    }
}
