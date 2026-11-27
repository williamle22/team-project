package use_case.check_eligibility;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibilityLineTest {
    private static final EligibilityLine CSC108 = new EligibilityLine("CSC108H1", MET, null, List.of());

    @Test
    void gettersReturnConstructorValues() {
        EligibilityLine line = new EligibilityLine("One of:", NOT_MET, "not taken", List.of(CSC108));

        assertEquals("One of:", line.getText());
        assertEquals(NOT_MET, line.getStatus());
        assertEquals(Optional.of("not taken"), line.getReason());
        assertEquals(List.of(CSC108), line.getChildren());
    }

    @Test
    void reasonIsOptional() {
        assertEquals(Optional.empty(), CSC108.getReason());
    }

    @Test
    void copiesChildren() {
        List<EligibilityLine> children = new ArrayList<>(List.of(CSC108));
        EligibilityLine line = new EligibilityLine("All of:", MET, null, children);

        children.add(CSC108);

        assertEquals(List.of(CSC108), line.getChildren());
        assertThrows(UnsupportedOperationException.class, () -> line.getChildren().add(CSC108));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new EligibilityLine(null, MET, null, List.of()));
        assertThrows(NullPointerException.class, () -> new EligibilityLine("a", null, null, List.of()));
        assertThrows(NullPointerException.class, () -> new EligibilityLine("a", MET, null, null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(EligibilityLine.class)
                .withPrefabValues(EligibilityLine.class, CSC108, new EligibilityLine("b", NOT_MET, null, List.of()))
                .withNonnullFields("text", "status", "children")
                .verify();
    }

    @Test
    void toStringShowsFields() {
        String s = new EligibilityLine("One of:", NOT_MET, "not taken", List.of(CSC108)).toString();

        assertTrue(s.contains("One of:"));
        assertTrue(s.contains("NOT_MET"));
        assertTrue(s.contains("not taken"));
        assertTrue(s.contains("CSC108H1"));
    }
}
