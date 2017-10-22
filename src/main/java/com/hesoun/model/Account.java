package com.hesoun.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Jakub Hesoun
 */
@Data
public class Account {
    //TODO JH Could this be replaced by non-static field?
    public static final int INITIAL_BALANCE = 30_000; //e.g.30_000
    private BigDecimal balance = BigDecimal.valueOf(INITIAL_BALANCE); //lever 2:1
    private BigDecimal allocation = BigDecimal.ZERO;
}
