package com.hesoun.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static com.hesoun.model.Account.INITIAL_BALANCE;

/**
 * Represents bought open long position of given stock.
 *
 * @author Jakub Hesoun
 */
@Builder
@Data
public class Position {
    private long id;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private Status status;
    private LocalDateTime buyDate;
    private LocalDateTime sellDate;
    private Slice slice;
    private String basketUUID;
    private Stock stock;

    public enum Status {
        OPEN("O"), CLOSED("C");

        Status(String symbol) {
            this.symbol = symbol;
        }

        private String symbol;

        public String getSymbol() {
            return symbol;
        }
    }


    public enum Slice {
        NO_SLICE(0, 0),
        TEN((int) (INITIAL_BALANCE * 0.1), 10),
        TWENTY((int) (INITIAL_BALANCE * 0.2), 20),
        THIRTY((int) (INITIAL_BALANCE * 0.3), 30),
        FOURTY((int) (INITIAL_BALANCE * 0.4), 40);

        private int amount;
        private int positionPercentage;
        private static List<Slice> sliceList = Arrays.asList(TEN, TWENTY, THIRTY, FOURTY);

        Slice(int amount, int positionPercentage) {
            this.amount = amount;
            this.positionPercentage = positionPercentage;
        }

        public int getAmount() {
            return amount;
        }

        public int getPositionPercentage() {
            return positionPercentage;
        }

        public static Slice getSliceFromPercentage(int positionPercentage) {
            for (Slice slice : Slice.values()) {
                if (slice.positionPercentage == positionPercentage) {
                    return slice;
                }
            }
            return NO_SLICE;
        }

        public static int getAmountFromPercentage(int positionPercentage) {
            for (Slice slice : Slice.values()) {
                if (slice.positionPercentage == positionPercentage) {
                    return slice.amount;
                }
            }
            return 0;
        }

        public Slice getNext() {
            if (EnumSet.of(NO_SLICE, FOURTY).contains(this)) {
                return NO_SLICE;
            }
            //leave out last item in the list
            for (int i = 0; i < sliceList.size() - 1; i++) {
                if (this == sliceList.get(i)) {
                    return sliceList.get(i + 1);
                }
            }
            return NO_SLICE; //should not happen
        }
    }
}
