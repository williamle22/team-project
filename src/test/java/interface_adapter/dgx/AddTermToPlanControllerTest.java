package interface_adapter.dgx;

import org.junit.jupiter.api.Test;
import use_case.add_term_to_plan.AddTermToPlanInputData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddTermToPlanControllerTest {
    private static final UUID PLAN_ID = UUID.randomUUID();

    private final List<AddTermToPlanInputData> inputs = new ArrayList<>();
    private final AddTermToPlanController controller = new AddTermToPlanController(PLAN_ID, inputs::add);

    @Test
    void sendsInputForBoundPlan() {
        controller.execute();

        assertEquals(List.of(new AddTermToPlanInputData(PLAN_ID)), inputs);
    }
}
