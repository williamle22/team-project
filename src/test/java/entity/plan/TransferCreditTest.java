package entity.plan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransferCreditTest {

    @Test
    void rejectsZeroOrNegativeWeight() {
        assertThrows(IllegalArgumentException.class, () -> new TransferCredit("MAT135H1", 0));
        assertThrows(IllegalArgumentException.class, () -> new TransferCredit("MAT135H1", -0.5));
    }
}
