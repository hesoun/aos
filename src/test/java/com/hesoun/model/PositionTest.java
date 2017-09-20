package com.hesoun.model;


import org.junit.Test;

import static com.hesoun.model.Position.Slice.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jakub Hesoun
 */
public class PositionTest {

    @Test
    public void getNextSlide() {
        assertThat(TEN.getNext()).isEqualTo(TWENTY);
        assertThat(TWENTY.getNext()).isEqualTo(THIRTY);
        assertThat(THIRTY.getNext()).isEqualTo(FOURTY);

        assertThat(FOURTY.getNext()).isEqualTo(NO_SLICE);
        assertThat(NO_SLICE.getNext()).isEqualTo(NO_SLICE);
    }
}
