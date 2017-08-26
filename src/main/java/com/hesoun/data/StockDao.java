package com.hesoun.data;

import com.hesoun.AosException;
import com.hesoun.Config;
import com.hesoun.model.HistoricalDailyPrice;
import com.hesoun.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dao for persisting Stock data.
 *
 * @author Jakub Hesoun
 */
public class StockDao {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private Connection conn;

    public StockDao(Config config) {
        conn = Database.INSTANCE.connect(config);
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

    /**
     * Returns a shallow stock, no entites are eagerly loaded.
     */
    public List<Stock> getAllStock() {
        List<Stock> stocks = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT id,symbol,name,exchange,currency_code,first_traded_date, inserted FROM stock")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Stock stock = Stock.builder()
                        .id(rs.getLong(1))
                        .symbol(rs.getString(2))
                        .name(rs.getString(3))
                        .exchange(rs.getString(4))
                        .currencyCode(rs.getString(5))
                        .firstTradedDate(LocalDate.from(rs.getDate(6).toLocalDate()))
                        .inserted(rs.getTimestamp(7).toLocalDateTime())
                        .build();
                stocks.add(stock);
            }
        } catch (SQLException e) {
            throw new AosException("Cannot load all the stocks from DB",e);
        }

        return stocks;
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

}
