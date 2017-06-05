package com.axibase.energinet.parsers;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;

public class TableParser
        implements Parser<String[][], String> {
    private static final Logger log = LoggerFactory.getLogger(TableParser.class);


    public String[][] parse(String source) {
        Date date = new Date();
        log.info("File: {} ms", new Date().getTime() - date.getTime());

        date = new Date();
        Document document;
        document = Jsoup.parse(source, "UTF-8");
        log.info("Parsing: {} ms", new Date().getTime() - date.getTime());

        date = new Date();
        Element tableElement = document.select("table").get(1);
        log.info("Table: {} ms", new Date().getTime() - date.getTime());

        date = new Date();
        Elements rowsElement = tableElement.select("tr");
        log.info("{} row(s): {} ms", rowsElement.size(), new Date().getTime() - date.getTime());

        date = new Date();

        int height = rowsElement.size();
        int width1 = rowsElement.get(0).select("th").size();
        int width2 = rowsElement.get(1).select("th").size();
        int width = rowsElement.get(2).select("td").size();

        String[][] table = new String[height][width];

        Elements headerElements1 = rowsElement.get(0).select("th");
        Elements headerElements2 = rowsElement.get(1).select("th");

        ArrayList<String> header1 = new ArrayList<>(width);
        ArrayList<String> header2 = new ArrayList<>(width);

        for (int c = 0; c < width1; c++) {
            String colspanString = headerElements1.get(c).attr("colspan");
            int colspan = StringUtils.isBlank(colspanString) ? 1 : Integer.parseInt(colspanString);

            String rowSpanString = headerElements1.get(c).attr("rowspan");
            int rowspan = StringUtils.isBlank(rowSpanString) ? 1 : Integer.parseInt(rowSpanString);

            for (int cc = 0; cc < colspan; cc++) {
                header1.add(headerElements1.get(c).text());
            }

            if (rowspan != 1) {
                header2.add("");
            }
        }

        for (int c = 0; c < width2; c++) {
            header2.add(headerElements2.get(c).text());
        }

        for (int c = 0; c < width; c++) {
            table[0][c] = header1.get(c);
            table[1][c] = header2.get(c);
        }
        log.info("Header: {} ms", new Date().getTime() - date.getTime());
        date = new Date();
        for (int r = 2; r < height; r++) {
            Elements colsElement = rowsElement.get(r).select("td");

            for (int c = 0; c < width; c++) {
                table[r][c] = colsElement.get(c).text().replace(",", "").replace(" ", " ");
            }
        }
        log.info("Data: {} ms", new Date().getTime() - date.getTime());
        return table;
    }
}

