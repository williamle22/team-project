package entity.plan;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransferCreditTest {

    @Test
    void rejectsZeroOrNegativeWeight() {
        assertThrows(IllegalArgumentException.class, () -> new TransferCredit("MAT135H1", 0));
        assertThrows(IllegalArgumentException.class, () -> new TransferCredit("MAT135H1", -0.5));
    }

    @Test
    void gettersReturnConstructorValues() {
        TransferCredit t = new TransferCredit("PHL100Y1", 1.0);
        assertEquals("PHL100Y1", t.getCode());
        assertEquals(1.0, t.getWeight());
    }

    @Test
    void toStringShowsCode() {
        assertTrue(new TransferCredit("PHL100Y1", 1.0).toString().contains("PHL100Y1"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(TransferCredit.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void rejectsNonFiniteWeight() {
        assertThrows(IllegalArgumentException.class, () -> new TransferCredit("MAT135H1", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new TransferCredit("MAT135H1", Double.POSITIVE_INFINITY));
    }
}
