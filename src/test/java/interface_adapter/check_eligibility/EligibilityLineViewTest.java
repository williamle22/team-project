package interface_adapter.check_eligibility;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibilityLineViewTest {
    private static final EligibilityLineView CSC108 = new EligibilityLineView("CSC108H1", "met", "", List.of());

    @Test
    void gettersReturnConstructorValues() {
        EligibilityLineView v = new EligibilityLineView("One of:", "not met", "not taken", List.of(CSC108));

        assertEquals("One of:", v.getText());
        assertEquals("not met", v.getStatus());
        assertEquals("not taken", v.getReason());
        assertEquals(List.of(CSC108), v.getChildren());
    }

    @Test
    void copiesChildren() {
        List<EligibilityLineView> children = new ArrayList<>(List.of(CSC108));
        EligibilityLineView v = new EligibilityLineView("All of:", "met", "", children);

        children.clear();

        assertEquals(List.of(CSC108), v.getChildren());
        assertThrows(UnsupportedOperationException.class, () -> v.getChildren().add(CSC108));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new EligibilityLineView(null, "met", "", List.of()));
        assertThrows(NullPointerException.class, () -> new EligibilityLineView("a", null, "", List.of()));
        assertThrows(NullPointerException.class, () -> new EligibilityLineView("a", "met", null, List.of()));
        assertThrows(NullPointerException.class, () -> new EligibilityLineView("a", "met", "", null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(EligibilityLineView.class)
                .withPrefabValues(EligibilityLineView.class, CSC108,
                        new EligibilityLineView("b", "review", "", List.of()))
                .withNonnullFields("text", "status", "reason", "children")
                .verify();
    }

    @Test
    void toStringShowsFields() {
        String s = new EligibilityLineView("One of:", "not met", "not taken", List.of(CSC108)).toString();

        assertTrue(s.contains("One of:"));
        assertTrue(s.contains("not met"));
        assertTrue(s.contains("not taken"));
        assertTrue(s.contains("CSC108H1"));
    }
}
