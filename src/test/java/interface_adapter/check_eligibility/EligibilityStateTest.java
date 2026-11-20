package interface_adapter.check_eligibility;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibilityStateTest {
    private static final CourseEligibilityView VIEW =
            new CourseEligibilityView(UUID.randomUUID(), "CSC148H1", "met", "Requirements met", List.of());

    @Test
    void defaultsToEmpty() {
        EligibilityState s = new EligibilityState();

        assertNull(s.getPlanId());
        assertEquals(Map.of(), s.getCourses());
        assertEquals(List.of(), s.getPlanWarnings());
        assertEquals("", s.getError());
    }

    @Test
    void settersUpdateFields() {
        UUID plan = UUID.randomUUID();
        EligibilityState s = new EligibilityState();

        s.setPlanId(plan);
        s.setCourses(Map.of(VIEW.getPlannedCourseId(), VIEW));
        s.setPlanWarnings(List.of("warn"));
        s.setError("error");

        assertEquals(plan, s.getPlanId());
        assertEquals(Map.of(VIEW.getPlannedCourseId(), VIEW), s.getCourses());
        assertEquals(List.of("warn"), s.getPlanWarnings());
        assertEquals("error", s.getError());
    }

    @Test
    void collectionsAreCopiedInOrder() {
        Map<UUID, CourseEligibilityView> courses = new LinkedHashMap<>();
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CourseEligibilityView v =
                    new CourseEligibilityView(UUID.randomUUID(), "CSC10" + i + "H1", "met", "", List.of());
            courses.put(v.getPlannedCourseId(), v);
            ids.add(v.getPlannedCourseId());
        }
        List<String> warnings = new ArrayList<>(List.of("a"));
        EligibilityState s = new EligibilityState();

        s.setCourses(courses);
        s.setPlanWarnings(warnings);
        courses.clear();
        warnings.clear();

        assertEquals(ids, new ArrayList<>(s.getCourses().keySet()));
        assertEquals(List.of("a"), s.getPlanWarnings());
        assertThrows(UnsupportedOperationException.class, () -> s.getCourses().clear());
        assertThrows(UnsupportedOperationException.class, () -> s.getPlanWarnings().add("b"));
    }

    @Test
    void toStringShowsCountAndError() {
        EligibilityState s = new EligibilityState();
        s.setCourses(Map.of(VIEW.getPlannedCourseId(), VIEW));
        s.setError("Course data is still loading.");

        assertTrue(s.toString().contains("1 courses"));
        assertTrue(s.toString().contains("Course data is still loading."));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(EligibilityState.class)
                .withPrefabValues(EligibilityLineView.class,
                        new EligibilityLineView("a", "met", "", List.of()),
                        new EligibilityLineView("b", "review", "", List.of()))
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}
