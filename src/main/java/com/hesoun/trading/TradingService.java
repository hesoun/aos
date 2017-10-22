package com.hesoun.trading;

import com.hesoun.extracting.Pair;
import com.hesoun.model.Position;
import com.hesoun.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

import static java.text.MessageFormat.format;

/**
 * @author Jakub Hesoun
 */
public class TradingService {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    //TODO JH these service can be changed to daos
    private final AccountService accountService;
    private final PositionService positionService;

    public TradingService(AccountService accountService, PositionService positionService) {
        this.accountService = accountService;
        this.positionService = positionService;
    }

    /**
     * Obtains a sorted set of possible buys sorted by RSI2 and select which one will be bought in
     * what slice. Lowest RSI2 should be selected but money management may not allow to buy next slice.
     *
     * @return a Pair of Stock and Slice to buy or null if no such pair exists for given day
     */
    public Pair<Stock, Position.Slice> selectAndGetBuyCandidate(SortedSet<Stock> possibleBuys) {
        Objects.requireNonNull(possibleBuys, "possible buys cannot be null");

        for (Stock stock : possibleBuys) {
            Position.Slice sliceToBuy = Position.Slice.TEN;
            if (stock.getOpenPositions() != null) {
                sliceToBuy = getBiggestSlice(stock.getOpenPositions()).getNext();
            }
            if (sliceToBuy == Position.Slice.NO_SLICE) {
                //this position has all slices possible, try the next stock with second lowest RSI2
                LOG.info("Stock {} is fully invested in. Trying the next possible candidate...", stock.getName());
                continue;
            }
            if (!positionService.canBuySlice(sliceToBuy)) {
                LOG.info("Cannot buy slice {} for stock {}. There is not enough resources. {} already allocated.", sliceToBuy.getAmount(), stock.getName(), accountService.getAllocation());
                //TODO JH shouldnt I try to buy smaller slice for the next possibleBuy
                return null;
            }
            return new Pair<>(stock, sliceToBuy);
        }
        return null;
    }

    private Position.Slice getBiggestSlice(List<Position> positions) {
        Position.Slice biggestSlice = Position.Slice.NO_SLICE;
        for (Position position : positions) {
            if (biggestSlice.compareTo(position.getSlice()) < 0) {
                biggestSlice = position.getSlice();
            }
        }
        return biggestSlice;
    }

    public void buySlice(Stock stock, Position.Slice slice, LocalDate buyDate) {
        Objects.requireNonNull(stock, format("stock {0} cannot be null", stock.getName()));
        Objects.requireNonNull(stock.getHistoricalDailyPrices(), format("historical daily prices for stock {0} cannot be null", stock.getName()));
        Objects.requireNonNull(slice, format("slice cannot be null for stock {0}", stock.getName()));

        if (!positionService.canBuySlice(slice)) {
            throw new IllegalArgumentException("Cannot buy slice " + slice.getAmount() + " for stock " + stock.getName() + ". There is not enough resources. " + accountService.getAllocation() + " already allocated.");
        }

        BigDecimal actualPrice = stock.getTodayPrices().getAdjustedClose();
        int numOfShares = BigDecimal.valueOf(slice.getAmount()).divide(actualPrice, BigDecimal.ROUND_DOWN).intValueExact();
        BigDecimal totalPrice = actualPrice.multiply(new BigDecimal(numOfShares));
        BigDecimal allocation = accountService.getAllocation();
        LOG.info("Buying a {} slice of {} for {}",slice, stock.getName(), actualPrice);
        Position position = Position.builder()
                .stock(stock)
                .buyDate(buyDate)
                .buyPrice(actualPrice)
                .slice(slice)
                .shares(numOfShares)
                .status(Position.Status.OPEN)
                .build();
        positionService.buyPosition(position);
        LOG.info("Bought");
        //TODO JH finish buying process
    }

}
