package use_case.check_eligibility;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseEligibilityTest {
    private static final UUID ID = UUID.randomUUID();
    private static final EligibilityLine LINE = new EligibilityLine("Prerequisites", MET, null, List.of());

    @Test
    void gettersReturnConstructorValues() {
        CourseEligibility c = new CourseEligibility(ID, "CSC148H1", "Winter 2027", NOT_MET, List.of(LINE),
                List.of("not offered in Winter 2027"));

        assertEquals(ID, c.getPlannedCourseId());
        assertEquals("CSC148H1", c.getCode());
        assertEquals("Winter 2027", c.getPlacementLabel());
        assertEquals(NOT_MET, c.getOverall());
        assertEquals(List.of(LINE), c.getSections());
        assertEquals(List.of("not offered in Winter 2027"), c.getWarnings());
    }

    @Test
    void copiesLists() {
        List<EligibilityLine> sections = new ArrayList<>(List.of(LINE));
        List<String> warnings = new ArrayList<>(List.of("a"));
        CourseEligibility c = new CourseEligibility(ID, "CSC148H1", "Winter 2027", MET, sections, warnings);

        sections.add(LINE);
        warnings.add("b");

        assertEquals(List.of(LINE), c.getSections());
        assertEquals(List.of("a"), c.getWarnings());
        assertThrows(UnsupportedOperationException.class, () -> c.getWarnings().add("c"));
    }

    @Test
    void rejectsNulls() {
        List<EligibilityLine> s = List.of();
        List<String> w = List.of();

        assertThrows(NullPointerException.class, () -> new CourseEligibility(null, "a", "b", MET, s, w));
        assertThrows(NullPointerException.class, () -> new CourseEligibility(ID, null, "b", MET, s, w));
        assertThrows(NullPointerException.class, () -> new CourseEligibility(ID, "a", null, MET, s, w));
        assertThrows(NullPointerException.class, () -> new CourseEligibility(ID, "a", "b", null, s, w));
        assertThrows(NullPointerException.class, () -> new CourseEligibility(ID, "a", "b", MET, null, w));
        assertThrows(NullPointerException.class, () -> new CourseEligibility(ID, "a", "b", MET, s, null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(CourseEligibility.class)
                .withPrefabValues(EligibilityLine.class, LINE, new EligibilityLine("b", NOT_MET, null, List.of()))
                .withNonnullFields("plannedCourseId", "code", "placementLabel", "overall", "sections", "warnings")
                .verify();
    }

    @Test
    void toStringShowsFields() {
        String s = new CourseEligibility(ID, "CSC148H1", "Winter 2027", NOT_MET, List.of(LINE),
                List.of("not offered in Winter 2027")).toString();

        assertTrue(s.contains("CSC148H1"));
        assertTrue(s.contains("Winter 2027"));
        assertTrue(s.contains("NOT_MET"));
        assertTrue(s.contains("Prerequisites"));
        assertTrue(s.contains("not offered in Winter 2027"));
    }
}
