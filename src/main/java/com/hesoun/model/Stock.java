package com.hesoun.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.text.MessageFormat.format;

/**
 * Stock entity contains metadata about particular stock.
 *
 * @author Jakub Hesoun
 */
@Builder
@Data
@ToString(exclude = "openPositions")
@EqualsAndHashCode(of = {"id", "symbol", "exchange"})
public class Stock {
    private long id;
    private String symbol;
    private String name;
    private String exchange;
    private String currencyCode;
    private LocalDate firstTradedDate;
    private LocalDateTime inserted;
    private List<HistoricalDailyPrice> historicalDailyPrices;
    private List<Position> openPositions;
    //not persisted @Transient from JPA would be more accurate
    private transient Indicators currentIndicators;

    /**
     * @return first element in {@link #historicalDailyPrices} list. It should always point to the prices for current buyDate
     */
    public HistoricalDailyPrice getTodayPrices() {
        if (historicalDailyPrices.isEmpty()) {
            throw new IllegalStateException(format("historical daily prices is empty for stock {0}", name));
        }

        return historicalDailyPrices.get(0);
    }

    public void addOpenPosition(Position position) {
        openPositions.add(position);
    }


}
