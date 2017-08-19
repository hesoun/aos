package com.hesoun;

import com.hesoun.model.HistoricalDailyPrice;
import com.hesoun.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Dao for persting Stock data.
 *
 * @author Jakub Hesoun
 */
public class StockDao {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private static Connection conn;

    public StockDao() {
        if (conn == null) {
            conn = new Database().connect();
        }
    }

    public Stock persist(Stock stock) {
        LOG.info("Starting to persist {} with {} days of price data", stock.getName(), stock.getHistoricalDailyPrice().size());
        persistStockFlat(stock);
        int i = 0;
        for (HistoricalDailyPrice price : stock.getHistoricalDailyPrice()) {
            price.setStockId(stock.getId());
            persistHistoricalDailyPriceFlat(price);
            if (++i % 1000 == 0) {
                LOG.info("Persisted {} days of price data for {}", i, stock.getName());
            }
        }
        return stock;
    }

    private Stock persistStockFlat(Stock stock) {
        try (PreparedStatement st = conn.prepareStatement("INSERT INTO stock (symbol,name,exchange,currency_code,first_traded_date,inserted) " +
                "VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, stock.getSymbol());
            st.setString(2, stock.getName());
            st.setString(3, stock.getExchange());
            st.setString(4, stock.getCurrencyCode());
            st.setDate(5, Date.valueOf(stock.getFirstTradedDate()));
            st.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                throw new AosException("Inserting stock " + stock.getName() + " failed.");
            }
            try (ResultSet generatedKeys = st.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    stock.setId(id);
                } else {
                    throw new AosException("Inserting stock " + stock.getName() + " failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            throw new AosException("Cannot persist stock " + stock.getName(), e);
        }

        return stock;
    }

    private HistoricalDailyPrice persistHistoricalDailyPriceFlat(HistoricalDailyPrice price) {
        try (PreparedStatement st = conn.prepareStatement("INSERT INTO historical_eod_price " +
                "(date,open,close,high,low,volume,adjclose,unadjclose,stock_id) " +
                "VALUES (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            st.setTimestamp(1, Timestamp.valueOf(price.getDate()));
            st.setBigDecimal(2, price.getOpen());
            st.setBigDecimal(3, price.getClose());
            st.setBigDecimal(4, price.getHigh());
            st.setBigDecimal(5, price.getLow());
            st.setInt(6, price.getVolume());
            st.setBigDecimal(7, price.getAdjustedClose());
            st.setBigDecimal(8, price.getUnadjustedClose());
            st.setLong(9, price.getStockId());

            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                LOG.error("Inserting historical daily price {} failed", price);
                throw new AosException("Inserting stock failed");
            }
            try (ResultSet generatedKeys = st.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    price.setId(id);
                } else {
                    LOG.error("Inserting price {} failed. No ID obtained", price);
                    throw new AosException("Inserting stock failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            LOG.error("Exception when persisting {}, {}", price, e);
            throw new AosException("Cannot persist price");
        }

        return price;
    }

    private class Database {
        private final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/postgres";
        private final String DB_USER = "postgres";
        private final String DB_PASSWORD = "postgres";

        public Connection connect() {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                LOG.error("Driver class has not been found");
                throw new AosException(e);
            }

            Connection conn = null;
            try {
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            } catch (SQLException e) {
                LOG.error("Unable to obtain connection to the database");
                throw new AosException(e);
            }

            if (conn == null) {
                LOG.error("Unable to obtain connection to the database");
                throw new AosException("Unable to obtain connection to the database");
            }
            return conn;
        }
    }
}
