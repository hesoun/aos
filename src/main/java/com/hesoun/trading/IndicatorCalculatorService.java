package com.hesoun.trading;

import com.hesoun.model.HistoricalDailyPrice;
import com.hesoun.model.Indicators;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Jakub Hesoun
 */
public class IndicatorCalculatorService {
    private List<HistoricalDailyPrice> priceList;

    public IndicatorCalculatorService(List<HistoricalDailyPrice> priceList) {
        //TODO check if prices are ordered
//        for(HistoricalDailyPrice price : priceList) {
//            if(price.getBuyDate().isBefore()) {
//                throw new IllegalArgumentException("Price list is not ordered by buyDate descendi")
//            }
//            LocalDateTime lastDate = price.getBuyDate();
//        }
        this.priceList = priceList;
    }

    public Indicators calculateIndicators() {
        BigDecimal sma200 = calculateSimpleMovingAverage(200);
        BigDecimal rsi2 = calculateRSI(2);
        BigDecimal sma5 = calculateSimpleMovingAverage(5);
        return new Indicators(sma200, rsi2, sma5);
    }

    public BigDecimal calculateSimpleMovingAverage(int periodLength) {
        if (periodLength > priceList.size()) {
            throw new IllegalArgumentException("Cannot calculate SMA for " + periodLength + " days when there is only " + priceList.size() + " prices");
        }
        return priceList.stream()
                .map(HistoricalDailyPrice::getAdjustedClose)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(periodLength), BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal calculateRSI(int periodLength) {
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal loses = BigDecimal.ZERO;
        BigDecimal lastPrice = priceList.get(periodLength).getAdjustedClose();
        for (int i = periodLength - 1; i >= 0; i--) {
            BigDecimal actualPrice = priceList.get(i).getAdjustedClose();
            if (actualPrice.compareTo(lastPrice) > 0) {
                gains = gains.add(actualPrice.subtract(lastPrice));
            } else {
                loses = loses.add(lastPrice.subtract(actualPrice));
            }
            lastPrice = actualPrice;
        }
        if (gains.compareTo(BigDecimal.ZERO) == 0) {
            //no gains, RSI = 0
            return BigDecimal.ZERO;
        } else if (loses.compareTo(BigDecimal.ZERO) == 0) {
            //no loses RSI = 1
            return BigDecimal.ONE;
        }
        //100 - 100/(1 + (avg gains/ avg loses))
        BigDecimal avgGains = gains.divide(BigDecimal.valueOf(periodLength), BigDecimal.ROUND_HALF_UP);
        BigDecimal avgLoses = loses.divide(BigDecimal.valueOf(periodLength), BigDecimal.ROUND_HALF_UP);
        BigDecimal relativeStrength = avgGains.divide(avgLoses, BigDecimal.ROUND_HALF_UP);
        BigDecimal fraction = BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(relativeStrength), BigDecimal.ROUND_HALF_UP);
        return BigDecimal.valueOf(100).subtract(fraction);


    }
}
