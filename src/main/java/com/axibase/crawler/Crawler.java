package com.axibase.crawler;

import com.axibase.crawler.atsd.DataProvider;
import com.axibase.crawler.atsd.PriceStatistics;
import com.axibase.crawler.common.Config;
import com.axibase.crawler.common.Logger;
import com.axibase.crawler.common.Result;
import com.axibase.crawler.crawling.ProductsCrawler;
import com.axibase.crawler.data.*;
import com.axibase.crawler.orders.OrderInfo;
import com.axibase.crawler.orders.OrderInfoParser;
import com.axibase.crawler.urlsearching.ProductUrlParser;
import com.axibase.crawler.urlsearching.UrlSearcher;

import java.util.*;

class Crawler {

    void fullUpdate(Config config, String productUrlsFile) {

        Result<OrdersSet> ordersSetResult = getOrdersSet(config.ordersDirectory);
        if (ordersSetResult.errorText != null) {
            Logger.log(ordersSetResult.errorText);
            return;
        }
        HashMap<Integer, OrderInfo> productsMap = ordersSetResult.result.products;

        DataProvider provider = new DataProvider(config.queryHost, config.user, config.password, config.tcpHost, config.tcpPort);
        Result<Boolean> providerStartResult = provider.start();
        if (providerStartResult.errorText != null) {
            Logger.log(providerStartResult.errorText);
            return;
        }

        Result<List<ProductUrl>> newProductUrlsResult = searchUrls(productsMap.values(), productUrlsFile, false);
        if (newProductUrlsResult.errorText != null) {
            Logger.log(newProductUrlsResult.errorText);
            return;
        }
        List<ProductUrl> productUrls = newProductUrlsResult.result;

        if (config.baseUrls.length == 0) {
            Logger.log("No base urls");
            return;
        }

        Result<HashMap<Integer, PriceStatistics>> priceStatisticsResult = provider.getPriceStatistics();
        if (priceStatisticsResult.errorText != null) {
            Logger.log(priceStatisticsResult.errorText);
            return;
        }
        Map<Integer, PriceStatistics> priceStatistics = priceStatisticsResult.result;

        searchProducts(
                productUrls,
                config.baseUrls,
                productsMap,
                priceStatistics,
                ordersSetResult.result.productsInlastOrder,
                provider);

        provider.stop();
    }

    void scanProducts(Config config, String productUrlsFile) {

        Result<OrdersSet> ordersSetResult = getOrdersSet(config.ordersDirectory);
        if (ordersSetResult.errorText != null) {
            Logger.log(ordersSetResult.errorText);
            return;
        }

        DataProvider provider = new DataProvider(config.queryHost, config.user, config.password, config.tcpHost, config.tcpPort);
        Result<Boolean> providerStartResult = provider.start();
        if (providerStartResult.errorText != null) {
            Logger.log(providerStartResult.errorText);
            return;
        }

        ProductUrlParser productUrlParser = new ProductUrlParser();
        Result<ArrayList<ProductUrl>> productUrlsResult = productUrlParser.readProductUrls(productUrlsFile);
        if (productUrlsResult.errorText != null) {
            Logger.log(productUrlsResult.errorText);
            return;
        }
        List<ProductUrl> productUrls = productUrlsResult.result;

        Result<HashMap<Integer, PriceStatistics>> priceStatisticsResult = provider.getPriceStatistics();
        if (priceStatisticsResult.errorText != null) {
            Logger.log(priceStatisticsResult.errorText);
            return;
        }
        Map<Integer, PriceStatistics> priceStatistics = priceStatisticsResult.result;

        searchProducts(
                productUrls,
                config.baseUrls,
                ordersSetResult.result.products,
                priceStatistics,
                ordersSetResult.result.productsInlastOrder,
                provider);

        provider.stop();
    }

    void scanPrices(Config config, String productUrlsFile) {

        Result<OrdersSet> ordersSetResult = getOrdersSet(config.ordersDirectory);
        if (ordersSetResult.errorText != null) {
            Logger.log(ordersSetResult.errorText);
            return;
        }
        HashMap<Integer, OrderInfo> productsMap = ordersSetResult.result.products;

        DataProvider provider = new DataProvider(config.queryHost, config.user, config.password, config.tcpHost, config.tcpPort);
        Result<Boolean> providerStartResult = provider.start();
        if (providerStartResult.errorText != null) {
            Logger.log(providerStartResult.errorText);
            return;
        }

        ProductUrlParser productUrlParser = new ProductUrlParser();
        Result<ArrayList<ProductUrl>> productUrlsResult = productUrlParser.readProductUrls(productUrlsFile);
        if (productUrlsResult.errorText != null) {
            Logger.log(productUrlsResult.errorText);
            return;
        }
        List<ProductUrl> productUrls = productUrlsResult.result;

        HashSet<Integer> knownProductIds = new HashSet<>(productUrls.size());
        for (ProductUrl productUrl : productUrls) {
            if (productUrl == null) continue;
            knownProductIds.add(productUrl.id);
        }

        ArrayList<OrderInfo> unknownOrders = new ArrayList<>();
        for (OrderInfo orderInfo : productsMap.values()) {
            if (orderInfo == null) continue;
            if (knownProductIds.contains(orderInfo.productId)) continue;
            unknownOrders.add(orderInfo);
        }

        Result<HashMap<Integer, PriceStatistics>> priceStatisticsResult = provider.getPriceStatistics();
        if (priceStatisticsResult.errorText != null) {
            Logger.log(priceStatisticsResult.errorText);
            return;
        }
        Map<Integer, PriceStatistics> priceStatisticsMap = priceStatisticsResult.result;

        ProductsCrawler crawler = new ProductsCrawler();

        if (unknownOrders.size() > 0) {

            Result<List<ProductUrl>> newProductUrlsResult = searchUrls(unknownOrders, productUrlsFile, true);
            if (newProductUrlsResult.errorText != null) {
                Logger.log(newProductUrlsResult.errorText);
                return;
            }
            List<ProductUrl> newProductUrls = newProductUrlsResult.result;

            searchProducts(
                    newProductUrls,
                    config.baseUrls,
                    productsMap,
                    priceStatisticsMap,
                    ordersSetResult.result.productsInlastOrder,
                    provider);
        }

        int productIndex = 0;
        for (ProductUrl productUrl : productUrls) {
            productIndex++;
            Logger.log("Searching product price %1s/%2s", productIndex, productUrls.size());

            if (productUrl.relativeUrl == null) {
                Logger.log("Skip searching product %1s/%2s", productIndex, productUrls.size());
                continue;
            }

            PriceStatistics priceStatistics = priceStatisticsMap.get(productUrl.id);

            OrderInfo orderInfo = productsMap.get(productUrl.id);
            if (orderInfo == null) {
                Logger.log("Error searching order for %1s", productUrl.name);
                continue;
            }

            Double lastPrice;

            for (String baseUrl : config.baseUrls) {
                Result<PriceData> priceDataResult = crawler.getPriceData(baseUrl, productUrl);

                if (priceDataResult.errorText != null) {
                    Logger.log("Error searching product price \"%1s\": \"%2s\"", productUrl.name, priceDataResult.errorText);
                }

                PriceData priceData = priceDataResult.result;

                lastPrice = priceData.price;

                if (lastPrice == null) {
                    if (priceStatistics != null) {
                        lastPrice = priceStatistics.lastPrice;
                    }
                }

                if (lastPrice == null) {
                    lastPrice = orderInfo.originalPrice;
                }

                Result<Boolean> updateResult = provider.updatePrice(productUrl.id, lastPrice, priceData.discount, priceData.zone);
                if (updateResult.errorText != null) {
                    Logger.log("Error updating database for product \"%1s\": \"%2s\"", productUrl.name, updateResult.errorText);
                }
            }

            provider.updateOrderInfo(
                    productUrl.id,
                    orderInfo.originalPrice,
                    orderInfo.amount,
                    orderInfo.date,
                    ordersSetResult.result.productsInlastOrder.contains(productUrl.id));
        }

        provider.stop();
    }

    private Result<List<ProductUrl>> searchUrls(Collection<OrderInfo> orders, String productUrlsFile, boolean append) {
        ProductUrlParser parser = new ProductUrlParser();
        UrlSearcher searcher = new UrlSearcher();
        List<ProductUrl> productUrls = searcher.searchUrls(orders);
        Result<Boolean> writeResult = parser.writeProductUrls(productUrls, productUrlsFile, append);
        if (writeResult.errorText != null) {
            return new Result<>(writeResult.errorText, null);
        }

        return new Result<>(null, productUrls);
    }

    private void searchProducts(
            List<ProductUrl> productUrls,
            String[] baseUrls,
            Map<Integer, OrderInfo> ordersMap,
            Map<Integer, PriceStatistics> priceStatisticsSet,
            Set<Integer> productsInLastOrder,
            DataProvider provider) {

        ProductsCrawler crawler = new ProductsCrawler();
        int productIndex = 0;
        for (ProductUrl productUrl : productUrls) {
            productIndex++;
            Logger.log("Searching product info and price %1s/%2s", productIndex, productUrls.size());

            if (productUrl.relativeUrl == null) {
                Logger.log("Skip searching product %1s/%2s", productIndex, productUrls.size());
                continue;
            }

            Result<ProductInfo> productInfoResult = null;
            for (String baseUrl : baseUrls) {
                productInfoResult = crawler.getProductInfo(baseUrl, productUrl);
                if (productInfoResult.errorText == null) {
                    break;
                }
            }

            if (productInfoResult == null) {
                Logger.log("Error searching product info \"%1s\"", productUrl.name);
                continue;
            }

            if (productInfoResult.errorText != null || productInfoResult.result == null) {
                Logger.log("Error searching product info \"%1s\": \"%2s\"", productUrl.name, productInfoResult.errorText);
                continue;
            }

            ArrayList<PriceData> priceDatas = new ArrayList<>();
            for (String baseUrl : baseUrls) {
                Result<PriceData> priceDataResult = crawler.getPriceData(baseUrl, productUrl);
                if (priceDataResult.errorText != null) {
                    Logger.log("Error searching product price \"%1s\": \"%2s\"", productUrl.name, priceDataResult.errorText);
                }

                if (priceDataResult.result != null) {
                    priceDatas.add(priceDataResult.result);
                }
            }

            OrderInfo orderInfo = ordersMap.get(productUrl.id);
            if (orderInfo == null) {
                Logger.log("No such order \"%1s\"", productUrl.name);
                continue;
            }

            PriceStatistics priceStatistics = priceStatisticsSet.get(productUrl.id);

            Double lastPrice = null;
            Double lastDiscount = null;
            if (priceDatas.size() > 0) {
                PriceData firstData = priceDatas.get(0);
                lastPrice = firstData.price;
                lastDiscount = firstData.discount;
            }

            if (priceStatistics != null) {
                if (lastPrice == null) {
                    lastPrice = priceStatistics.lastPrice;
                }

                if (lastDiscount == null) {
                    lastDiscount = priceStatistics.lastDiscount;
                }
            }

            if (lastPrice == null) {
                lastPrice = orderInfo.originalPrice;
            }

            if (lastDiscount == null) {
                lastDiscount = 0.0;
            }

            Result<Boolean> productUpdateResult = provider.updateProduct(
                    new ProductData(
                            productInfoResult.result,
                            priceDatas,
                            orderInfo.units,
                            productsInLastOrder.contains(orderInfo.productId),
                            lastPrice,
                            lastDiscount));
            if (productUpdateResult.errorText != null) {
                Logger.log("Error updating database for product \"%1s\": \"%2s\"", productUrl.name, productUpdateResult.errorText);
            }

            Result<Boolean> orderUpdateResult = provider.updateOrderInfo(
                    orderInfo.productId,
                    orderInfo.originalPrice,
                    orderInfo.amount,
                    orderInfo.date,
                    productsInLastOrder.contains(productUrl.id));

            if (orderUpdateResult.errorText != null) {
                Logger.log("Error updating database for product \"%1s\": \"%2s\"", productUrl.name, productUpdateResult.errorText);
            }
        }
    }

    private Result<OrdersSet> getOrdersSet(String ordersDirectory) {
        OrderInfoParser orderInfoParser = new OrderInfoParser();

        Result<ArrayList<OrderData>> ordersResult = orderInfoParser.readOrders(ordersDirectory);
        if (ordersResult.errorText != null) {
            return new Result<>(ordersResult.errorText, null);
        }
        List<OrderData> orders = ordersResult.result;

        OrderData lastOrder = null;

        for (OrderData orderData : orders) {
            if (lastOrder == null) {
                lastOrder = orderData;
                continue;
            }

            if (orderData.date.after(lastOrder.date)) {
                lastOrder = orderData;
            }
        }

        HashSet<Integer> productsInLastOrder = new HashSet<>();
        if (lastOrder != null && lastOrder.products != null) {

            for (OrderInfo orderInfo : lastOrder.products) {
                productsInLastOrder.add(orderInfo.productId);
            }
        }

        HashMap<Integer, OrderInfo> ordersMap = new HashMap<>();
        for (OrderData orderData : orders) {
            for (OrderInfo orderInfo : orderData.products) {
                OrderInfo existingOrderInfo = ordersMap.get(orderInfo.productId);

                if (existingOrderInfo == null || existingOrderInfo.date.before(orderInfo.date)) {
                    ordersMap.put(orderInfo.productId, orderInfo);
                    continue;
                }

                if (existingOrderInfo.date.equals(orderData.date)) {
                    ordersMap.put(
                            orderInfo.productId,
                            new OrderInfo(
                                    existingOrderInfo.date,
                                    existingOrderInfo.productId,
                                    existingOrderInfo.productName,
                                    existingOrderInfo.amount + orderInfo.amount,
                                    existingOrderInfo.originalPrice,
                                    existingOrderInfo.units
                            ));
                }
            }
        }

        return new Result<>(null, new OrdersSet(ordersMap, productsInLastOrder));
    }

    private class OrdersSet {

        final HashMap<Integer, OrderInfo> products;

        final HashSet<Integer> productsInlastOrder;

        private OrdersSet(HashMap<Integer, OrderInfo> products, HashSet<Integer> productsInlastOrder) {
            this.products = products;
            this.productsInlastOrder = productsInlastOrder;
        }
    }
}
