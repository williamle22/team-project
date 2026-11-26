package use_case.add_course_to_plan;

import entity.plan.Plan;
import entity.term.AcademicTerm;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddCourseToPlanOutputDataTest {
    private static final UUID PLAN_ID = UUID.randomUUID();

    @Test
    void gettersReturnPlanCopyAndMessage() {
        Plan plan = planWithFallTerm();
        AddCourseToPlanOutputData data = new AddCourseToPlanOutputData(plan, "Course added.");

        assertEquals(plan, data.getPlan());
        assertNotSame(plan, data.getPlan());
        assertEquals("Course added.", data.getMessage());
    }

    @Test
    void copiesPlanWhenConstructedAndWhenReturned() {
        Plan plan = planWithFallTerm();
        AddCourseToPlanOutputData data = new AddCourseToPlanOutputData(plan, "Course added.");
        plan.rename("Changed original");

        Plan returned = data.getPlan();
        assertEquals("Test plan", returned.getName());
        returned.rename("Changed returned copy");
        assertEquals("Test plan", data.getPlan().getName());
    }

    @Test
    void rejectsNullMessage() {
        assertThrows(NullPointerException.class, () -> new AddCourseToPlanOutputData(planWithFallTerm(), null));
    }

    @Test
    void nullPlanIsPreserved() {
        AddCourseToPlanOutputData data = new AddCourseToPlanOutputData(null, "Plan unavailable.");

        assertEquals(null, data.getPlan());
        assertEquals("Plan unavailable.", data.getMessage());
    }

    @Test
    void equalValuesHaveEqualHashCodes() {
        Plan firstPlan = planWithFallTerm();
        Plan samePlan = Plan.restore(PLAN_ID, "Test plan", null, List.of(), List.of(AcademicTerm.fall(2026)),
                List.of(), List.of());
        AddCourseToPlanOutputData first = new AddCourseToPlanOutputData(firstPlan, "Course added.");
        AddCourseToPlanOutputData same = new AddCourseToPlanOutputData(samePlan, "Course added.");

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertEquals(first, first);
        assertNotEquals(first, new AddCourseToPlanOutputData(samePlan, "Different message."));
        assertNotEquals(first, new AddCourseToPlanOutputData(
                Plan.restore(UUID.randomUUID(), "Test plan", null, List.of(), List.of(AcademicTerm.fall(2026)),
                        List.of(), List.of()), "Course added."));
        assertNotEquals(first, "other");
    }

    @Test
    void nullPlanOutputDataHasValueEquality() {
        AddCourseToPlanOutputData first = new AddCourseToPlanOutputData(null, "Plan unavailable.");
        AddCourseToPlanOutputData same = new AddCourseToPlanOutputData(null, "Plan unavailable.");
        AddCourseToPlanOutputData differentMessage = new AddCourseToPlanOutputData(null, "Different message.");

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, differentMessage);
    }

    @Test
    void toStringShowsMessage() {
        assertTrue(new AddCourseToPlanOutputData(planWithFallTerm(), "Course added.")
                .toString().contains("Course added."));
    }

    private static Plan planWithFallTerm() {
        return Plan.restore(PLAN_ID, "Test plan", null, List.of(), List.of(AcademicTerm.fall(2026)),
                List.of(), List.of());
    }
}
