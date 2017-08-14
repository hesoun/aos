package com.hesoun.yahoo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author Jakub Hesoun
 */
public class Chart {
    private String symbol;
    private List<LocalDateTime> dates;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<LocalDateTime> getDates() {
        return Collections.unmodifiableList(dates);
    }

    public void setDates(List<LocalDateTime> dates) {
        this.dates = dates;
    }
}
