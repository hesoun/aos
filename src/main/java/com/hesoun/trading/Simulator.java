package com.hesoun.trading;

import com.hesoun.Config;
import com.hesoun.data.HistoricalDailyPriceDao;
import com.hesoun.data.PositionDao;
import com.hesoun.data.StockDao;
import com.hesoun.extracting.Pair;
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
    //TODO JH replace daos with services
    private final StockDao stockDao;
    private final HistoricalDailyPriceDao priceDao;
    private final TradingService tradingService;
    private final PositionService positionService;

    private List<Stock> stocks;

    public Simulator(Config config) throws SQLException {
        this.config = config;
        this.stockDao = new StockDao(config);
        this.priceDao = new HistoricalDailyPriceDao(config);
        PositionDao positionDao = new PositionDao(config);
        AccountService accountService = new AccountService(config, positionDao);
        positionService = new PositionService(positionDao);
        this.tradingService = new TradingService(accountService, positionService);
    }

    public void simulate() throws SQLException {
        stocks = stockDao.getAllStock();

        LocalDate today = config.getFrom();
        final int periodLength = 200;
        while (today.isBefore(config.getTo())) {
            //skip non-trading days
            if (!isTradingDay(today)) {
                today = today.plusDays(1);
                continue;
            }
            SortedSet<Stock> possibleBuys = new TreeSet<>(new RSIComparator());

            //simulate daily trading session for all the stock -> fills possibleBuys Set
            simulateDailyTradingSession(today, possibleBuys, periodLength);

            Pair<Stock, Position.Slice> buyWinner = tradingService.selectAndGetBuyCandidate(possibleBuys);
            if (buyWinner == null) {
                LOG.info("Not buying anything on {}", today);
            } else {
                LOG.info("Buy candidate on {} is {} with price {}, sma200 {} and rsi2 {}", today, buyWinner.getLeft().getName(), buyWinner.getLeft().getTodayPrices().getAdjustedClose(),
                        buyWinner.getLeft().getCurrentIndicators().getSma200(), buyWinner.getLeft().getCurrentIndicators().getRsi2());

                tradingService.buySlice(buyWinner.getLeft(), buyWinner.getRight(), today);
            }

            possibleBuys.clear();
            today = today.plusDays(1);
        }

    }

    private void simulateDailyTradingSession(LocalDate today, SortedSet<Stock> possibleBuys, int periodLength) {
        LOG.info("Simulating a trading session on {}...", today);
        //possible stock to buy ordered by RSI
        Map<Stock, Stock> stocksWithOpenPositionMap = stockDao.getStockWithOpenPosition();

        for (Stock stock : stocks) {
//            LOG.info("Loading stock prices for {} on {} for last {} days", stock.getName(), today, periodLength);
            //TODO JH get historical daily prices for all stock in one GO instead of getting it for every stock
            List<HistoricalDailyPrice> priceList = priceDao.getLastPricesForStock(stock.getId(), periodLength, today);
            if (priceList.size() != periodLength) {
                LOG.debug("There are only {} prices for {} but {} requested", priceList.size(), stock.getName(), periodLength);
                continue; //not enough data
            }
            stock.setHistoricalDailyPrices(priceList);
            IndicatorCalculatorService service = new IndicatorCalculatorService(priceList);
            BigDecimal actualPrice = priceList.get(0).getAdjustedClose();
            Indicators indicators = service.calculateIndicators();
            if (stocksWithOpenPositionMap.containsKey(stock)) {
                List<Position> openPositions = stocksWithOpenPositionMap.get(stock).getOpenPositions();
                stock.setOpenPositions(openPositions);
                if (actualPrice.compareTo(indicators.getSma5()) > 0) {
                    positionService.sellOpenPositions(stock, today);
                }
                //TODO JH add to possible buys to buy additional slice
            }
            stock.setCurrentIndicators(indicators);
            if (actualPrice.compareTo(indicators.getSma200()) > 0) {
                //add possible buy if price is bigger than 200day moving average
                possibleBuys.add(stock);
            }

//            LOG.info("SMA200 for {} on {} is {} ", stock.getName(), today, indicators.getSma200());
//            LOG.info("RSI(2) for {} is {}", stock.getName(), indicators.getRsi2());
        }
    }

    private boolean isTradingDay(LocalDate date) throws SQLException {
        if (EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.getDayOfWeek())) {
            return false;
        }
        return priceDao.hasPriceForGivenDate(date);
    }
}
