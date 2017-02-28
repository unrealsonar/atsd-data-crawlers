package com.axibase.crawler.urlsearching;

import com.axibase.crawler.common.Result;
import com.axibase.crawler.data.ProductUrl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProductUrlParser {
    public Result<ArrayList<ProductUrl>> readProductUrls(String filePath) {
        ArrayList<ProductUrl> productUrls = new ArrayList<>();
        try {
            try (FileReader fileReader = new FileReader(filePath)) {
                try (BufferedReader reader = new BufferedReader(fileReader)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] elements = line.split("\t");

                        if (elements.length < 3) {
                            return new Result<>("Incorrect product urls file format", null);
                        }

                        int productId;
                        try {
                            productId = Integer.parseInt(elements[0]);
                        } catch (NumberFormatException ex) {
                            return new Result<>("Incorrect product urls file format", null);
                        }

                        String productName = elements[1];
                        String productUrl = elements[2].equals("null") ? null : elements[2];

                        productUrls.add(new ProductUrl(productId, productName, productUrl));
                    }
                }
            }
        } catch (IOException ex) {
            return new Result<>("Unable to read product urls file", null);
        }

        return new Result<>(null, productUrls);
    }

    public Result<Boolean> writeProductUrls(List<ProductUrl> productUrls, String outputFile, boolean append) {
        try {
            try (FileWriter streamWriter = new FileWriter(outputFile, append)) {
                try (BufferedWriter writer = new BufferedWriter(streamWriter)) {
                    for (ProductUrl product : productUrls) {
                        if (product == null) continue;
                        String urlString = product.relativeUrl != null ? product.relativeUrl : "null";
                        writer.write(String.format("%1s\t%2s\t%3s", product.id, product.name, urlString));
                        writer.newLine();
                    }
                }
            }
        } catch (IOException ex) {
            return new Result<>(String.format("Unable to write data to file %1s", outputFile), false);
        }

        return new Result<>(null, true);
    }
}
