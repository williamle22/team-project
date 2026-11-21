package entity.evaluation;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExclusionReasonTest {

    @Test
    void gettersReturnConstructorValues() {
        ExclusionReason r = new ExclusionReason("excluded by CSC111H1", ExclusionResult.EXCLUDED);

        assertEquals("excluded by CSC111H1", r.getText());
        assertEquals(ExclusionResult.EXCLUDED, r.getResult());
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new ExclusionReason(null, ExclusionResult.REVIEW));
        assertThrows(NullPointerException.class, () -> new ExclusionReason("permission", null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(ExclusionReason.class).withNonnullFields("text", "result").verify();
    }

    @Test
    void toStringShowsFields() {
        String s = new ExclusionReason("excluded by CSC111H1", ExclusionResult.EXCLUDED).toString();

        assertTrue(s.contains("EXCLUDED"));
        assertTrue(s.contains("excluded by CSC111H1"));
    }
}
