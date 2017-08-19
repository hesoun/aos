package com.hesoun;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Jakub Hesoun
 */
@EqualsAndHashCode
@Getter
public class Pair<L, R> {

    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }
}
