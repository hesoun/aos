package com.hesoun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jakub Hesoun
 */
public class Downloader {

    private static final Logger LOG = LoggerFactory.getLogger(Downloader.class);
    private static final String YAHOO = "http://query2.finance.yahoo.com/v8/finance/chart/MSFT";


    public static void main(String... args) throws IOException {
        new Downloader().run();
    }

    public void run() throws IOException {

//        fetchDataFromFile();
//
//
        CloseableHttpClient httpClient = HttpClients.createDefault();

        LocalDate from = LocalDate.of(2016, 8, 1);
        LocalDate to = LocalDate.of(2016, 7, 1);

        String query = "?period1=" + from.toEpochDay() + "&period2=" + to.toEpochDay() + "&interval=1d";
        HttpGet get = new HttpGet(YAHOO + query);

        CloseableHttpResponse response = httpClient.execute(get);
        try {
            LOG.info("status line: {}", response.getStatusLine());
            LOG.info("entity: {}", EntityUtils.toString(response.getEntity()));
        } finally {
            httpClient.close();
        }
    }

    public void fetchDataFromFile() throws IOException {
        String jsonString = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/data.json")))
                .lines()
                .collect(Collectors.joining("\n"));
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(jsonString);

        List<String> fieldNames = new ArrayList<>();
        List<LocalDateTime> timestamps = new ArrayList<>();
        while (!parser.isClosed()) {
            JsonToken token = parser.nextToken();
            if(JsonToken.FIELD_NAME.equals(token)) {
                String fieldName = parser.getCurrentName();
                fieldNames.add(fieldName);
                if("timestamp".equals(fieldName)) {
                    parser.nextToken();
                    while(!JsonToken.END_ARRAY.equals(parser.nextToken())) {
                        LocalDateTime datetime = Instant.ofEpochSecond(parser.getLongValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                        timestamps.add(LocalDateTime.from(datetime));
                    }
                }
            }
        }
        LOG.info("{}", fieldNames);
        LOG.info("{}", timestamps);

    }
}
