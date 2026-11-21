package use_case.check_eligibility;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckEligibilityOutputDataTest {
    private static final UUID PLAN = UUID.randomUUID();

    private static CourseEligibility course(String code) {
        return new CourseEligibility(UUID.randomUUID(), code, "Fall 2026", MET, List.of(), List.of());
    }

    @Test
    void gettersReturnConstructorValues() {
        CourseEligibility c = course("CSC148H1");
        CheckEligibilityOutputData data = new CheckEligibilityOutputData(PLAN, Map.of(c.getPlannedCourseId(), c),
                List.of("CSC148H1 appears 2 times (retake?)"));

        assertEquals(PLAN, data.getPlanId());
        assertEquals(Map.of(c.getPlannedCourseId(), c), data.getCourses());
        assertEquals(List.of("CSC148H1 appears 2 times (retake?)"), data.getPlanWarnings());
    }

    @Test
    void keepsCourseOrder() {
        Map<UUID, CourseEligibility> courses = new LinkedHashMap<>();
        List<UUID> ids = new ArrayList<>();
        for (String code : List.of("MAT137Y1", "CSC108H1", "STA130H1", "CSC148H1", "ANT100Y1")) {
            CourseEligibility c = course(code);
            courses.put(c.getPlannedCourseId(), c);
            ids.add(c.getPlannedCourseId());
        }

        CheckEligibilityOutputData data = new CheckEligibilityOutputData(PLAN, courses, List.of());

        assertEquals(ids, new ArrayList<>(data.getCourses().keySet()));
    }

    @Test
    void copiesCollections() {
        Map<UUID, CourseEligibility> courses = new LinkedHashMap<>();
        List<String> warnings = new ArrayList<>(List.of("a"));
        CheckEligibilityOutputData data = new CheckEligibilityOutputData(PLAN, courses, warnings);
        CourseEligibility c = course("CSC148H1");

        courses.put(c.getPlannedCourseId(), c);
        warnings.add("b");

        assertTrue(data.getCourses().isEmpty());
        assertEquals(List.of("a"), data.getPlanWarnings());
        assertThrows(UnsupportedOperationException.class, () -> data.getCourses().put(c.getPlannedCourseId(), c));
        assertThrows(UnsupportedOperationException.class, () -> data.getPlanWarnings().add("c"));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new CheckEligibilityOutputData(null, Map.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new CheckEligibilityOutputData(PLAN, null, List.of()));
        assertThrows(NullPointerException.class, () -> new CheckEligibilityOutputData(PLAN, Map.of(), null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(CheckEligibilityOutputData.class)
                .withPrefabValues(EligibilityLine.class, new EligibilityLine("a", MET, null, List.of()),
                        new EligibilityLine("b", REVIEW, null, List.of()))
                .withNonnullFields("planId", "courses", "planWarnings")
                .verify();
    }

    @Test
    void toStringShowsFields() {
        CourseEligibility c = new CourseEligibility(UUID.randomUUID(), "CSC148H1", "Fall 2026", REVIEW, List.of(),
                List.of());
        String s = new CheckEligibilityOutputData(PLAN, Map.of(c.getPlannedCourseId(), c), List.of("warn"))
                .toString();

        assertTrue(s.contains(PLAN.toString()));
        assertTrue(s.contains("CSC148H1"));
        assertTrue(s.contains("warn"));
    }
}
