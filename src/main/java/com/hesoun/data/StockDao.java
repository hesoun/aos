package com.hesoun.data;

import com.hesoun.AosException;
import com.hesoun.Config;
import com.hesoun.model.HistoricalDailyPrice;
import com.hesoun.model.Position;
import com.hesoun.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dao for persisting Stock data.
 *
 * @author Jakub Hesoun
 */
public class StockDao {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final Connection conn;

    public StockDao(Config config) {
        conn = Database.INSTANCE.connect(config);
    }

    /**
     * Insert stock entity into the database along with all the historical daily prices.
     */
    public Stock persist(Stock stock) {
        LOG.info("Starting to persist {} with {} days of price data", stock.getName(), stock.getHistoricalDailyPrices().size());
        persistStockFlat(stock);
        int i = 0;
        for (HistoricalDailyPrice price : stock.getHistoricalDailyPrices()) {
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
        try (PreparedStatement ps = conn.prepareStatement("SELECT id,symbol,name,exchange,currency_code,first_traded_date, inserted FROM stock")) {
            ResultSet rs = ps.executeQuery();
            List<Stock> stocks = new ArrayList<>();
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
            return stocks;
        } catch (SQLException e) {
            throw new AosException("Cannot load all the stocks from DB", e);
        }
    }

    /**
     * Retrieve a Map of stock to stock with openPosition field populated. A Map is used because Set do not contain
     * get method.
     */
    public Map<Stock, Stock> getStockWithOpenPosition() {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT s.id,s.symbol,s.name,s.exchange,s.currency_code,s.first_traded_date,s.inserted," +
                        "p.id,p.buy_price,p.status,p.buy_date,p.slice,p.shares,p.basket_uuid " +
                        "FROM stock AS s " +
                        "LEFT JOIN position AS p ON s.id = p.stock_id " +
                        "WHERE status = ?::STATUS_TYPE " +
                        "ORDER BY s.id,p.slice")) {
            ps.setString(1, "O");
            ResultSet rs = ps.executeQuery();
            Map<Stock, Stock> stocksMap = new HashMap<>();
            while (rs.next()) {
                Stock stock = Stock.builder()
                        .id(rs.getLong(1))
                        .symbol(rs.getString(2))
                        .name(rs.getString(3))
                        .exchange(rs.getString(4))
                        .currencyCode(rs.getString(5))
                        .firstTradedDate(rs.getDate(6).toLocalDate())
                        .inserted(rs.getTimestamp(7).toLocalDateTime())
                        .openPositions(new ArrayList<>())
                        .build();
                if (stocksMap.containsKey(stock)) {
                    stock = stocksMap.get(stock);
                }

                List<Position> positionList = stock.getOpenPositions();
                Position position = Position.builder()
                        .id(rs.getLong(8))
                        .buyPrice(rs.getBigDecimal(9))
                        .status(Position.Status.getStatusFromSymbol(rs.getString(10)))
                        .buyDate(rs.getDate(11).toLocalDate())
                        .slice(Position.Slice.getSliceFromPercentage(rs.getInt(12)))
                        .shares(rs.getInt(13))
                        .basketUUID(rs.getString(14))
                        .stock(stock)
                        .build();
                positionList.add(position);
                stocksMap.put(stock, stock);
            }
            return stocksMap;
        } catch (SQLException e) {
            throw new AosException("Cannot load open positions from DB", e);
        }
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
