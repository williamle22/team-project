package interface_adapter.check_graduation;

import org.junit.jupiter.api.Test;
import use_case.check_graduation.CheckGraduationInputData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckGraduationControllerTest {

    @Test
    void wrapsPlanIdAndMode() {
        List<CheckGraduationInputData> inputs = new ArrayList<>();
        UUID id = UUID.randomUUID();
        CheckGraduationController controller = new CheckGraduationController(inputs::add);

        controller.execute(id, true);
        controller.execute(id, false);

        assertEquals(List.of(new CheckGraduationInputData(id, true), new CheckGraduationInputData(id, false)),
                inputs);
    }
}
