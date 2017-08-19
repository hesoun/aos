package com.hesoun.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents various prices and volume traded in given date for particular stock
 *
 * @author Jakub Hesoun
 */
@Builder
@Data
public class HistoricalDailyPrice {
    private long id;
    private LocalDateTime date;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;

    private int volume;

    private BigDecimal adjustedClose;
    private BigDecimal unadjustedClose;

    private long stockId;
}
