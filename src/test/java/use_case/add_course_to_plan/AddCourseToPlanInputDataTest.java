package use_case.add_course_to_plan;

import entity.term.AcademicTerm;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddCourseToPlanInputDataTest {
    private static final UUID PLAN_ID = UUID.randomUUID();

    @Test
    void gettersReturnConstructorValues() {
        AcademicTerm term = AcademicTerm.summer(2027);
        AddCourseToPlanInputData input = new AddCourseToPlanInputData(PLAN_ID, "CSC148H1", term);

        assertEquals(PLAN_ID, input.getPlanId());
        assertEquals("CSC148H1", input.getCourseCode());
        assertEquals(term, input.getTargetTerm());
    }

    @Test
    void rejectsNullPlanIdAndCourseCode() {
        assertThrows(NullPointerException.class,
                () -> new AddCourseToPlanInputData(null, "CSC148H1", AcademicTerm.fall(2026)));
        assertThrows(NullPointerException.class,
                () -> new AddCourseToPlanInputData(PLAN_ID, null, AcademicTerm.fall(2026)));
    }

    @Test
    void allowsMissingTargetTerm() {
        AddCourseToPlanInputData input = new AddCourseToPlanInputData(PLAN_ID, "CSC148H1", null);

        assertNull(input.getTargetTerm());
    }

    @Test
    void equalsAndHashCodeUseEveryField() {
        AddCourseToPlanInputData first = new AddCourseToPlanInputData(PLAN_ID, "CSC148H1", AcademicTerm.fall(2026));
        AddCourseToPlanInputData same = new AddCourseToPlanInputData(PLAN_ID, "CSC148H1", AcademicTerm.fall(2026));
        AddCourseToPlanInputData differentTerm = new AddCourseToPlanInputData(PLAN_ID, "CSC148H1",
                AcademicTerm.winter(2027));
        AddCourseToPlanInputData differentPlan = new AddCourseToPlanInputData(UUID.randomUUID(), "CSC148H1",
                AcademicTerm.fall(2026));
        AddCourseToPlanInputData differentCourse = new AddCourseToPlanInputData(PLAN_ID, "CSC209H1",
                AcademicTerm.fall(2026));

        assertEquals(first, first);
        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, differentTerm);
        assertNotEquals(first, differentPlan);
        assertNotEquals(first, differentCourse);
        assertNotEquals(first, "other");

        AddCourseToPlanInputData missingTerm = new AddCourseToPlanInputData(PLAN_ID, "CSC148H1", null);
        AddCourseToPlanInputData sameMissingTerm = new AddCourseToPlanInputData(PLAN_ID, "CSC148H1", null);
        assertEquals(missingTerm, sameMissingTerm);
        assertEquals(missingTerm.hashCode(), sameMissingTerm.hashCode());
        assertNotEquals(missingTerm, first);
    }

    @Test
    void toStringShowsCourseCode() {
        assertTrue(new AddCourseToPlanInputData(PLAN_ID, "CSC148H1", AcademicTerm.fall(2026))
                .toString().contains("CSC148H1"));
    }
}
