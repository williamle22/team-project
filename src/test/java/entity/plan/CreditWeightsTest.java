package entity.plan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreditWeightsTest {

    @Test
    void fromCodeHalfAndFull() {
        assertEquals(0.5, CreditWeights.FROM_CODE.weightOf("CSC148H1"));
        assertEquals(1.0, CreditWeights.FROM_CODE.weightOf("PHL100Y1"));
        assertEquals(0.5, CreditWeights.FROM_CODE.weightOf("MATA31H3"));
    }

    @Test
    void fromCodeTreatsShortCodeAsHalf() {
        assertEquals(0.5, CreditWeights.FROM_CODE.weightOf("PHL1Y"));
    }
}
