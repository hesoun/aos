package com.hesoun;

import com.hesoun.model.Stock;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Main class which responsibility is to load all S&P stock names and for every one of them obtain data from given endpoint,
 * parse it and persist into DB.
 *
 * @author Jakub Hesoun
 */
public class DataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(DataExtractor.class);
    private static final String YAHOO_URL = "http://query2.finance.yahoo.com/v8/finance/chart/";
    private final LocalDateTime from = LocalDateTime.of(1997, 1, 1, 0, 0);
    private final LocalDateTime to = LocalDateTime.of(2017, 8, 15, 0, 0);

    private StockDao dao = new StockDao();

    public static void main(String... args) throws IOException {
        new DataExtractor().run();
    }

    private void run() throws IOException {
        //fill S&P100 components
        List<Pair<String, String>> sp100Components = SP100Components.load();

        CloseableHttpClient httpClient = HttpClients.createDefault();

        for (Pair<String, String> component : sp100Components) {
            long startTime = System.currentTimeMillis();
            try {
                getProcessAndSaveData(httpClient, component);
            } catch (Exception e) {
                //if an error happens when processing, log it and proceed with the next stock
                LOG.error("Persisting of data for stock {} failed.", component.getRight(), e);
                continue;
            }
            LOG.info("All data for {} has been saved into DB in {}ms", component.getRight(), System.currentTimeMillis() - startTime);
        }
    }

    private void getProcessAndSaveData(CloseableHttpClient httpClient, Pair<String, String> component) throws IOException {
        LOG.info("Starting to get and process data for {} between {} and {}", component.getRight(), from, to);
        String query = component.getLeft() + "?period1=" + from.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond() + "&period2=" + to.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond() + "&interval=1d";
        HttpGet get = new HttpGet(YAHOO_URL + query);

        String json;
        //fetch stock and prices from yahoo
        try (CloseableHttpResponse response = httpClient.execute(get)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                LOG.info("Data obtained from Yahoo endpoint.");
            }
            json = EntityUtils.toString(response.getEntity());
        }
        //extract data from JSON reponse
        Stock stock = JsonHelper.parseJsonAndCreateStock(json, component);
        //persist into DB
        dao.persist(stock);

    }
}
