package com.hesoun.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Stock entity contains metadat about particular stock.
 * @author Jakub Hesoun
 */
@Builder
@Data
public class Stock {
    private long id;
    private String symbol;
    private String name;
    private String exchange;
    private String currencyCode;
    private LocalDate firstTradedDate;
    private LocalDateTime inserted;
    private List<HistoricalDailyPrice> historicalDailyPrice;
}
