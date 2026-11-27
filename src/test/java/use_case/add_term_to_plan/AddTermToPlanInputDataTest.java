package use_case.add_term_to_plan;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddTermToPlanInputDataTest {
    private static final UUID PLAN_ID = UUID.randomUUID();

    @Test
    void getterReturnsPlanId() {
        assertEquals(PLAN_ID, new AddTermToPlanInputData(PLAN_ID).getPlanId());
    }

    @Test
    void rejectsNullPlanId() {
        assertThrows(NullPointerException.class, () -> new AddTermToPlanInputData(null));
    }

    @Test
    void comparesByPlanId() {
        AddTermToPlanInputData input = new AddTermToPlanInputData(PLAN_ID);
        AddTermToPlanInputData same = new AddTermToPlanInputData(PLAN_ID);

        assertEquals(input, input);
        assertEquals(input, same);
        assertEquals(input.hashCode(), same.hashCode());
        assertNotEquals(input, new AddTermToPlanInputData(UUID.randomUUID()));
        assertNotEquals(input, "other");
    }

    @Test
    void toStringShowsPlanId() {
        assertTrue(new AddTermToPlanInputData(PLAN_ID).toString().contains(PLAN_ID.toString()));
    }
}
