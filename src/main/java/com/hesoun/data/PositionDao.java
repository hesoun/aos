package com.hesoun.data;

import com.hesoun.AosException;
import com.hesoun.Config;
import com.hesoun.model.Position;

import java.math.BigDecimal;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDate;

/**
 * @author Jakub Hesoun
 */
public class PositionDao {
    private final Connection connection;

    public PositionDao(Config config) {
        this.connection = Database.INSTANCE.connect(config);
    }

    /**
     * Get the total amount of all the open positions.
     */
    public int getTotalAllocation() {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT slice " +
                        "FROM position " +
                        "WHERE status = 'O'::STATUS_TYPE")) {
            ResultSet rs = ps.executeQuery();
            int totalAllocation = 0;
            while (rs.next()) {
                totalAllocation += Position.Slice.getAmountFromPercentage(rs.getInt(1));
            }
            return totalAllocation;

        } catch (SQLException e) {
            throw new AosException("Cannot load open positions from DB", e);
        }
    }

    /**
     * Insert position into DB with respective buyDate and buyPrice
     */
    public void buyPosition(Position position) {
        //TODO JH set basketId and strategyId
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO position (buy_price, status, buy_date, slice,shares, stock_id) " +
                        "VALUES (?,?::STATUS_TYPE,?,?::SLICE_TYPE,?, ?)")) {
            ps.setBigDecimal(1, position.getBuyPrice());
            ps.setString(2, Position.Status.OPEN.getSymbol());
            ps.setDate(3, Date.valueOf(position.getBuyDate()));
            ps.setString(4, position.getSlice().getPositionPercentageAsString());
            ps.setLong(5, position.getShares());
            ps.setLong(6, position.getStock().getId());

            ps.execute();
        } catch (SQLException e) {
            throw new AosException(MessageFormat.format("Cannot persist slice {0} for stock {1}", position.getSlice().getAmount(), position.getStock().getName()), e);
        }
    }

    /**
     * Performs DB update of a position that is already bought. This operation will change its status to CLOSED and
     * adds sellPrice and sellDate.
     */
    public void sellPosition(Position position, BigDecimal sellPrice, LocalDate sellDate) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE position SET sell_price=?,sell_date=?,status=?::STATUS_TYPE " +
                        "WHERE id=?")) {
            ps.setBigDecimal(1, sellPrice);
            ps.setDate(2, Date.valueOf(sellDate));
            ps.setString(3, "C");
            ps.setLong(4, position.getId());

            ps.execute();
        } catch (SQLException e) {
            throw new AosException(MessageFormat.format("Cannot sell position {0} for stock {1}", position.getSlice().getAmount(), position.getStock().getName()), e);
        }
    }
}
