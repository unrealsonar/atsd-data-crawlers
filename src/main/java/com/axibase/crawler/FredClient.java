package com.axibase.crawler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FredClient {
    private static final String BASE_URL = "https://api.stlouisfed.org/";

    private static final String CHILDREN_CATEGORIES = "fred/category/children";
    private static final String SERIES_TAGS = "fred/series/tags";
    private static final String CATEGORY_SERIES = "fred/category/series";
    private static final String SERIES_OBSERVATIONS = "fred/series/observations";
    private static final String SERIES_CATEGORIES = "fred/series/categories";
    private static final String CATEGORY = "fred/category";

    private ObjectMapper mapper = new ObjectMapper();
    private JsonFactory jsonFactory = mapper.getFactory();
    private Client client = Client.create();
    private WebResource resource;

    FredClient() {
        resource = client
                .resource(BASE_URL)
                .queryParam("api_key", "")
                .queryParam("file_type", "json");
    }

    private JsonNode readJsonResponse(ClientResponse response) {
        String jsonString = response.getEntity(String.class);
        try {
            JsonParser parser = jsonFactory.createParser(jsonString);
            return mapper.readTree(parser);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JsonNode requestWithParam(String path, String key, String value) {
        ClientResponse response = resource.path(path)
                .queryParam(key, value)
                .accept("application/json")
                .get(ClientResponse.class);

        return readJsonResponse(response);
    }

    int[] subCategories(int categoryId) {
        JsonNode json = requestWithParam(
                CHILDREN_CATEGORIES,
                "category_id",
                String.valueOf(categoryId)
        ).path("categories");

        int size = json.size();
        int[] result = new int[size];

        for (int i = 0; i < size; i++) {
            result[i] = json.get(i).get("id").asInt();
        }

        return result;
    }

    String[] seriesTags(String seriesId) {
        JsonNode json = requestWithParam(
                SERIES_TAGS,
                "series_id",
                seriesId
        ).path("tags");

        int size = json.size();
        String[] result = new String[size];

        for (int i = 0; i < size; i++) {
            result[i] = json.get(i).get("name").asText();
        }

        return result;
    }

    FredSeries[] categorySeries(int categoryId) {
        JsonNode json = requestWithParam(
                CATEGORY_SERIES,
                "category_id",
                String.valueOf(categoryId)
        ).path("seriess");

        try {
            return mapper.treeToValue(json, FredSeries[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    FredObservation[] seriesObservations(String seriesId) {
        JsonNode json = requestWithParam(
                SERIES_OBSERVATIONS,
                "series_id",
                seriesId
        ).path("observations");

        List<FredObservation> result = new ArrayList<>();

        int len = json.size();
        for (int i = 0; i < len; i++) {
            JsonNode singleObservation = json.get(i);
            try {
                FredObservation obs = mapper.treeToValue(singleObservation, FredObservation.class);
                result.add(obs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        FredObservation[] arrayResult = new FredObservation[result.size()];
        result.toArray(arrayResult);
        return arrayResult;
    }

    FredCategory[] seriesCategories(String seriesId) {
        JsonNode json = requestWithParam(
                SERIES_CATEGORIES,
                "series_id",
                seriesId
        ).path("categories");

        try {
            return mapper.treeToValue(json, FredCategory[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    FredCategory getCategory(int categoryId) {
        JsonNode json = requestWithParam(
                CATEGORY,
                "category_id",
                String.valueOf(categoryId)
        ).path("categories");

        try {
            return mapper.treeToValue(json, FredCategory[].class)[0];
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
