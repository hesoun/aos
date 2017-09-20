package com.hesoun.data;

import com.hesoun.AosException;
import com.hesoun.Config;
import com.hesoun.model.HistoricalDailyPrice;

import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakub Hesoun
 */
//TODO extract connection to commonDao and make it thread-safe
public class HistoricalDailyPriceDao {
    private Connection conn;

    public HistoricalDailyPriceDao(Config config) {
        conn = Database.INSTANCE.connect(config);
    }

    /**
     * Gets a list of {@link HistoricalDailyPrice}s for given stock. The list contains {@param days} number of prices
     * from days before {@param date} which are ordered by this date descending.
     */
    public List<HistoricalDailyPrice> getLastPricesForStock(long stockId, int days, LocalDate lastDate) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id,date,open,high,low,close,volume,adjclose,unadjclose,stock_id " +
                        "FROM historical_eod_price " +
                        "WHERE stock_id = ? AND date <= ? " +
                        "ORDER BY date DESC " +
                        "LIMIT ?")) {
            ps.setLong(1, stockId);
            ps.setDate(2, Date.valueOf(lastDate));
            ps.setInt(3, days);

            ResultSet rs = ps.executeQuery();
            List<HistoricalDailyPrice> result = new ArrayList<>(days);
            while (rs.next()) {
                HistoricalDailyPrice price = HistoricalDailyPrice.builder()
                        .id(rs.getInt(1))
                        .date(rs.getTimestamp(2).toLocalDateTime())
                        .open(rs.getBigDecimal(3))
                        .high(rs.getBigDecimal(4))
                        .low(rs.getBigDecimal(5))
                        .close(rs.getBigDecimal(6))
                        .volume(rs.getInt(7))
                        .adjustedClose(rs.getBigDecimal(8))
                        .unadjustedClose(rs.getBigDecimal(9))
                        .build();
                result.add(price);
            }
            return result;
        } catch (SQLException e) {
            throw new AosException(
                    MessageFormat.format("Cannot get {0} last days of prices going back from {1} for stock with id={2}", days, lastDate, stockId));
        }
    }

    /**
     * Returns true if any {@link HistoricalDailyPrice} entity exists with given{@param date}.
     * This method should help to skip non-trading days.
     *
     * @return id of given entry or -1 if not found
     */
    public boolean hasPriceForGivenDate(LocalDate date) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM historical_eod_price " +
                        "WHERE date = ? ")) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }
}
