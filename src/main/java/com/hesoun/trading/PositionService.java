package com.hesoun.trading;

import com.hesoun.data.PositionDao;
import com.hesoun.model.Account;
import com.hesoun.model.Position;
import com.hesoun.model.Position.Status;
import com.hesoun.model.Stock;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Handles buying and selling position.
 *
 * @author Jakub Hesoun
 */
public class PositionService {
    private final PositionDao positionDao;

    public PositionService(PositionDao positionDao) {
        this.positionDao = positionDao;
    }

    /**
     * Buys a position of given stock. It means that it inserts a row in DB with specific buy date and
     * buy price.
     */
    public void buyPosition(Position position) {
        Objects.requireNonNull(position, "position cannot be null");

        positionDao.buyPosition(position);
    }

    /**
     * Sells all open position for given {@param stock} on {@param today}.
     * It performs DB update and adds sell date and sell price to given row. Also state is changes to {@link Status.CLOSED}
     */
    public void sellOpenPositions(Stock stock, LocalDate today) {
        for (Position position : stock.getOpenPositions()) {
            positionDao.sellPosition(position, stock.getTodayPrices().getAdjustedClose(),today);
        }
    }

    /**
     * Checks if money management allows to buy given slice. If not, another stock will have to be tried.
     */
    public boolean canBuySlice(Position.Slice slice) {
        int totalAllocation = positionDao.getTotalAllocation();
        return slice.getAmount() + totalAllocation <= Account.INITIAL_BALANCE;
    }


}
