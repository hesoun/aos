package com.hesoun.trading;

import com.hesoun.Config;
import com.hesoun.data.HistoricalDailyPriceDao;
import com.hesoun.data.StockDao;
import com.hesoun.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * @author Jakub Hesoun
 */
public class Simulator {
    private static final Logger LOG = LoggerFactory.getLogger(Simulator.class);

    private final Config config;
    private final StockDao stockDao;
    private final HistoricalDailyPriceDao priceDao;

    private List<Stock> stocks;

    public Simulator(Config config) throws SQLException {
        this.config = config;
        this.stockDao = new StockDao(config);
        this.priceDao = new HistoricalDailyPriceDao(config);
    }

    public void simulate() throws SQLException {
        stocks = stockDao.getAllStock();

        LocalDate from = config.getFrom();
        final int periodLength = 200;
        while (from.isBefore(config.getTo())) {
            //skip non-trading days
            if (!isTradingDay(from)) {
                from = from.plusDays(1);
                continue;
            }
            //possible stock to buy ordered by RSI
            Map<Stock, List<Position>> stockOpenPositionMap = stockDao.getStockWithOpenPosition();
            SortedSet<Stock> possibleBuys = new TreeSet<>(new RSIComparator());

            //simulate daily trading session for all the stock
            simulateDailyTradingSession(from, stockOpenPositionMap, possibleBuys, periodLength);

            //get stock with lowest rsi2
            Stock buyCandidate = possibleBuys.last();
            LOG.info("Buy candidate on {} is {} with price {}, sma200 {} and rsi2 {}", from, buyCandidate.getName(), buyCandidate.getHistoricalDailyPrice().get(0).getAdjustedClose(), buyCandidate.getCurrentIndicators().getSma200(), buyCandidate.getCurrentIndicators().getRsi2());
            //TODO JH check if money management allows to buy a stock and if so, buy slice

            possibleBuys.clear();
            from = from.plusDays(1);
        }

    }

    private void simulateDailyTradingSession(LocalDate from, Map<Stock, List<Position>> stockOpenPositionMap, SortedSet<Stock> possibleBuys, int periodLength) {
        LOG.info("Simulating a trading session on {}", from);
        for (Stock stock : stocks) {
            LOG.info("Loading stock prices for {} on {} for last {} days", stock.getName(), from, periodLength);
            List<HistoricalDailyPrice> priceList = priceDao.getLastPricesForStock(stock.getId(), periodLength, from);
            if (priceList.size() != periodLength) {
                LOG.warn("There are only {} prices for {} but {} requested", priceList.size(), stock.getName(), periodLength);
                continue; //not enough data
            }
            stock.setHistoricalDailyPrice(priceList);
            IndicatorCalculatorService service = new IndicatorCalculatorService(priceList);
            BigDecimal actualPrice = priceList.get(0).getAdjustedClose();
            Indicators indicators = service.calculateIndicators();
            if (stockOpenPositionMap.keySet().contains(stock)) {
                List<Position> openPositions = stockOpenPositionMap.get(stock);
                //TODO JH check if price is > SMA5 and if position should be closed
                //TODO JH add to possible buys to buy additional slice
            }
            stock.setCurrentIndicators(indicators);
            if (actualPrice.compareTo(indicators.getSma200()) > 0) {
                //add possible buy if price is bigger than 200day moving average
                possibleBuys.add(stock);
            }

            LOG.info("SMA200 for {} on {} is {} ", stock.getName(), from,indicators.getSma200());
            LOG.info("RSI(2) for {} is {}", stock.getName(),indicators.getRsi2());
        }
    }

    private boolean isTradingDay(LocalDate date) throws SQLException {
        if (EnumSet.of(DayOfWeek.SATURDAY,DayOfWeek.SUNDAY).contains(date.getDayOfWeek())) {
            return false;
        }
        return priceDao.hasPriceForGivenDate(date);
    }
}
