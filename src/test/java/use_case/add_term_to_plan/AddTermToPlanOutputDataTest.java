package use_case.add_term_to_plan;

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

class AddTermToPlanOutputDataTest {
    private static final UUID PLAN_ID = UUID.randomUUID();

    @Test
    void gettersReturnPlanCopyAndMessage() {
        Plan plan = plan();
        AddTermToPlanOutputData data = new AddTermToPlanOutputData(plan, "Added term.");

        assertEquals(plan, data.getPlan());
        assertNotSame(plan, data.getPlan());
        assertEquals("Added term.", data.getMessage());
    }

    @Test
    void copiesPlanAtConstructionAndWhenReturned() {
        Plan plan = plan();
        AddTermToPlanOutputData data = new AddTermToPlanOutputData(plan, "Added term.");
        plan.rename("Changed original");

        Plan returned = data.getPlan();
        assertEquals("Test plan", returned.getName());
        returned.rename("Changed returned copy");
        assertEquals("Test plan", data.getPlan().getName());
    }

    @Test
    void rejectsNullPlanAndMessage() {
        assertThrows(NullPointerException.class, () -> new AddTermToPlanOutputData(null, "Added term."));
        assertThrows(NullPointerException.class, () -> new AddTermToPlanOutputData(plan(), null));
    }

    @Test
    void comparesPlanAndMessage() {
        AddTermToPlanOutputData data = new AddTermToPlanOutputData(plan(), "Added term.");
        AddTermToPlanOutputData same = new AddTermToPlanOutputData(plan(), "Added term.");
        AddTermToPlanOutputData differentMessage = new AddTermToPlanOutputData(plan(), "Different message.");
        Plan differentPlan = Plan.restore(UUID.randomUUID(), "Test plan", null, List.of(),
                List.of(AcademicTerm.fall(2026)), List.of(), List.of());
        AddTermToPlanOutputData differentPlanData = new AddTermToPlanOutputData(differentPlan, "Added term.");

        assertEquals(data, data);
        assertEquals(data, same);
        assertEquals(data.hashCode(), same.hashCode());
        assertNotEquals(data, differentMessage);
        assertNotEquals(data, differentPlanData);
        assertNotEquals(data, "other");
    }

    @Test
    void toStringShowsMessage() {
        assertTrue(new AddTermToPlanOutputData(plan(), "Added term.").toString().contains("Added term."));
    }

    private static Plan plan() {
        return Plan.restore(PLAN_ID, "Test plan", null, List.of(), List.of(AcademicTerm.fall(2026)),
                List.of(), List.of());
    }
}
