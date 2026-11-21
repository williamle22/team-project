package interface_adapter.check_eligibility;

import org.junit.jupiter.api.Test;
import use_case.check_eligibility.CheckEligibilityInputData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckEligibilityControllerTest {

    @Test
    void wrapsPlanId() {
        List<CheckEligibilityInputData> inputs = new ArrayList<>();
        UUID id = UUID.randomUUID();

        new CheckEligibilityController(inputs::add).execute(id);

        assertEquals(List.of(new CheckEligibilityInputData(id)), inputs);
    }
}
