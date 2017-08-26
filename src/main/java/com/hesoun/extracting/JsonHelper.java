package com.hesoun.extracting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hesoun.AosException;
import com.hesoun.model.HistoricalDailyPrice;
import com.hesoun.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class which is responsible for parsing retrieved Yahoo Json and create {@link Stock}
 * from it.
 *
 * @author Jakub Hesoun
 */
public class JsonHelper {
    private static final Logger LOG = LoggerFactory.getLogger(JsonHelper.class);

    private JsonHelper() {
        throw new AssertionError("No JsonHelper instance for you! This is static helper class.");
    }

    public static Stock parseJsonAndCreateStock(String json, Pair<String, String> component) throws IOException {
        LOG.info("Parsing JSON response for {}", component.getRight());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(json, JsonNode.class);

        //check if there is no error
        JsonNode errorNode = node.get("chart").get("error");
        if (!errorNode.isNull()) {
            String errorCode = errorNode.get("code").asText();
            String errorDescription = errorNode.get("description").asText();
            throw new AosException("Error when obtaining data for " + component.getLeft() + ":\n " +
                    "errorCode=" + errorCode + "\n" +
                    "errorDescription=" + errorDescription);
        }

        //get first important subnode with data
        JsonNode dataNode = node.get("chart").get("result").get(0);

        //create stock from metadata
        int firstTradedDateSeconds = dataNode.findValue("firstTradeDate").asInt();
        Stock stock = Stock.builder()
                .exchange(dataNode.findValue("exchangeName").asText())
                .symbol(dataNode.findValue("symbol").asText())
                .currencyCode(dataNode.findValue("currency").asText())
                .firstTradedDate(Instant.ofEpochSecond(firstTradedDateSeconds).atZone(ZoneId.of("UTC")).toLocalDate())
                .build();

        //get timestamps
        List<LocalDateTime> localDateTimes = getLocalDateTimeList(dataNode, "timestamp");

        //get prices
        List<BigDecimal> lowPrices = getBigDecimalList(dataNode, "low");
        List<BigDecimal> highPrices = getBigDecimalList(dataNode, "high");
        List<BigDecimal> closePrices = getBigDecimalList(dataNode, "close");
        List<BigDecimal> openPrices = getBigDecimalList(dataNode, "open");
        List<BigDecimal> adjClosePrices = getBigDecimalList(dataNode, "adjclose");
        List<BigDecimal> unadjClosePrices = getBigDecimalList(dataNode, "unadjclose");

        //get volume
        List<Integer> volumes = getIntegerList(dataNode, "volume");

        List<HistoricalDailyPrice> dailyPrices = new ArrayList<>(lowPrices.size());
        for (int i = 0; i < lowPrices.size(); i++) {
            dailyPrices.add(HistoricalDailyPrice.builder()
                    .date(localDateTimes.get(i))
                    .low(lowPrices.get(i))
                    .high(highPrices.get(i))
                    .close(closePrices.get(i))
                    .open(openPrices.get(i))
                    .volume(volumes.get(i))
                    .adjustedClose(adjClosePrices.get(i))
                    .unadjustedClose(unadjClosePrices.get(i))
                    .build()
            );
        }

        stock.setHistoricalDailyPrice(dailyPrices);
        stock.setName(component.getRight());
        LOG.info("JSON parsed successfully");
        return stock;
    }


    private static List<BigDecimal> getBigDecimalList(JsonNode node, String fieldName) {
        JsonNode foundNode = findLastNode(node, fieldName);
        List<BigDecimal> bigDecimalList = new ArrayList<>(node.size());
        for (JsonNode n : foundNode) {
            if (!n.isDouble()) {
                throw new IllegalStateException("Parsed node " + fieldName + " contains value which is not double: " + n.asText());

            }
            bigDecimalList.add(BigDecimal.valueOf(n.asDouble()));
        }
        return bigDecimalList;
    }

    private static List<Integer> getIntegerList(JsonNode node, String fieldName) {
        JsonNode foundNode = findLastNode(node, fieldName);
        List<Integer> integerList = new ArrayList<>(node.size());
        for (JsonNode n : foundNode) {
            if (!n.isInt()) {
                throw new IllegalStateException("Parsed node " + fieldName + " contains value which is not int: " + n.asText());

            }
            integerList.add(n.asInt());
        }
        return integerList;
    }

    private static List<LocalDateTime> getLocalDateTimeList(JsonNode node, String fieldName) {
        JsonNode foundNode = findLastNode(node, fieldName);
        List<LocalDateTime> localDateTimeList = new ArrayList<>(node.size());
        for (JsonNode n : foundNode) {
            if (!n.isInt()) {
                throw new IllegalStateException("Parsed node " + fieldName + " contains value which is not int: " + n.asText());

            }
            LocalDateTime dateTime = Instant.ofEpochSecond(n.asInt()).atZone(ZoneId.of("UTC")).toLocalDateTime();
            localDateTimeList.add(dateTime);
        }
        return localDateTimeList;
    }

    /**
     * Handy method if you are looking for some node with given name but it has also sub-node with the same name and you
     * want the last one.
     * <p>
     * E.g.: quote : {adjprice: [{adjprice:55}]}
     */
    private static JsonNode findLastNode(JsonNode node, String fieldName) {
        JsonNode foundNode = node;
        do {
            JsonNode temp = foundNode.findValue(fieldName);
            if (temp == null) {
                break;
            }
            foundNode = temp;
        } while (true);

        return foundNode;
    }


}
