package entity.evaluation;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExclusionOutcomeTest {

    @Test
    void gettersReturnConstructorValues() {
        ExclusionOutcome o = new ExclusionOutcome(ExclusionResult.EXCLUDED, List.of("excluded by CSC111H1"),
                List.of("CSC111H1"));

        assertEquals(ExclusionResult.EXCLUDED, o.getResult());
        assertEquals(List.of("excluded by CSC111H1"), o.getReasons());
        assertEquals(List.of("CSC111H1"), o.getBlockingCourses());
    }

    @Test
    void copiesLists() {
        List<String> reasons = new ArrayList<>(List.of("a"));
        List<String> blocking = new ArrayList<>(List.of("CSC111H1"));
        ExclusionOutcome o = new ExclusionOutcome(ExclusionResult.EXCLUDED, reasons, blocking);

        reasons.add("b");
        blocking.add("CSC148H1");

        assertEquals(List.of("a"), o.getReasons());
        assertEquals(List.of("CSC111H1"), o.getBlockingCourses());
        assertThrows(UnsupportedOperationException.class, () -> o.getReasons().add("c"));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new ExclusionOutcome(null, List.of(), List.of()));
        assertThrows(NullPointerException.class,
                () -> new ExclusionOutcome(ExclusionResult.REVIEW, null, List.of()));
        assertThrows(NullPointerException.class,
                () -> new ExclusionOutcome(ExclusionResult.REVIEW, List.of(), null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(ExclusionOutcome.class).withNonnullFields("result", "reasons", "blockingCourses")
                .verify();
    }

    @Test
    void toStringShowsFields() {
        String s = new ExclusionOutcome(ExclusionResult.EXCLUDED, List.of("excluded by CSC111H1"),
                List.of("CSC111H1")).toString();

        assertTrue(s.contains("EXCLUDED"));
        assertTrue(s.contains("excluded by CSC111H1"));
        assertTrue(s.contains("CSC111H1"));
    }
}
