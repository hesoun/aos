package com.hesoun.model;

import com.hesoun.AosException;

import java.text.MessageFormat;
import java.util.Comparator;

/**
 * Compares two {@link Stock}s according to their RSI2.
 *
 * @author Jakub Hesoun
 */
public class RSIComparator implements Comparator<Stock> {
    @Override
    public int compare(Stock o1, Stock o2) {
        if (o1.getCurrentIndicators() == null || o2.getCurrentIndicators() == null) {
            throw new AosException(MessageFormat.format(
                    "Cannot compare stock when indicator is null.stock1={0},stock2={1}", o1, o2));
        }
        if (o1 == o2) {
            return 0;
        }
        return o1.getCurrentIndicators().getRsi2().compareTo(o2.getCurrentIndicators().getRsi2());
    }
}
