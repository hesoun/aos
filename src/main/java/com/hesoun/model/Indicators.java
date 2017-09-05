package com.hesoun.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Represents various indicators for particular stock. It is computed on the fly, not persisted.
 * @author Jakub Hesoun
 */
@Data
public class Indicators {
    private final BigDecimal sma200;
    private final BigDecimal rsi2;
    private final BigDecimal sma5;
}
