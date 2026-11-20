package interface_adapter.check_eligibility;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseEligibilityViewTest {
    private static final UUID ID = UUID.randomUUID();
    private static final EligibilityLineView LINE = new EligibilityLineView("Prerequisites", "met", "", List.of());

    @Test
    void gettersReturnConstructorValues() {
        CourseEligibilityView v = new CourseEligibilityView(ID, "CSC148H1", "not met", "Missing CSC108H1",
                List.of(LINE));

        assertEquals(ID, v.getPlannedCourseId());
        assertEquals("CSC148H1", v.getCode());
        assertEquals("not met", v.getStatus());
        assertEquals("Missing CSC108H1", v.getSummary());
        assertEquals(List.of(LINE), v.getLines());
    }

    @Test
    void copiesLines() {
        List<EligibilityLineView> lines = new ArrayList<>(List.of(LINE));
        CourseEligibilityView v = new CourseEligibilityView(ID, "CSC148H1", "met", "", lines);

        lines.clear();

        assertEquals(List.of(LINE), v.getLines());
        assertThrows(UnsupportedOperationException.class, () -> v.getLines().add(LINE));
    }

    @Test
    void rejectsNulls() {
        List<EligibilityLineView> l = List.of();

        assertThrows(NullPointerException.class, () -> new CourseEligibilityView(null, "a", "met", "", l));
        assertThrows(NullPointerException.class, () -> new CourseEligibilityView(ID, null, "met", "", l));
        assertThrows(NullPointerException.class, () -> new CourseEligibilityView(ID, "a", null, "", l));
        assertThrows(NullPointerException.class, () -> new CourseEligibilityView(ID, "a", "met", null, l));
        assertThrows(NullPointerException.class, () -> new CourseEligibilityView(ID, "a", "met", "", null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(CourseEligibilityView.class)
                .withPrefabValues(EligibilityLineView.class, LINE,
                        new EligibilityLineView("b", "review", "", List.of()))
                .withNonnullFields("plannedCourseId", "code", "status", "summary", "lines")
                .verify();
    }

    @Test
    void toStringShowsFields() {
        String s = new CourseEligibilityView(ID, "CSC148H1", "not met", "Missing CSC108H1", List.of(LINE))
                .toString();

        assertTrue(s.contains("CSC148H1"));
        assertTrue(s.contains("not met"));
        assertTrue(s.contains("Missing CSC108H1"));
        assertTrue(s.contains("Prerequisites"));
    }
}
