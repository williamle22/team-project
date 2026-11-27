package interface_adapter.dgx;

import entity.term.AcademicTerm;
import entity.term.Season;
import org.junit.jupiter.api.Test;
import use_case.add_course_to_plan.AddCourseToPlanInputData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AddCourseToPlanControllerTest {
    private static final UUID PLAN_ID = UUID.randomUUID();

    private final List<AddCourseToPlanInputData> inputs = new ArrayList<>();
    private final AddCourseToPlanController controller = new AddCourseToPlanController(PLAN_ID, inputs::add);

    @Test
    void convertsDropValuesToInputData() {
        controller.execute("CSC148H1", 2027, "summer");

        assertEquals(List.of(new AddCourseToPlanInputData(PLAN_ID, "CSC148H1",
                new AcademicTerm(Season.SUMMER, 2027))), inputs);
    }

    @Test
    void unknownSeasonDoesNotDispatchInput() {
        assertThrows(IllegalArgumentException.class, () -> controller.execute("CSC148H1", 2027, "spring"));
        assertEquals(List.of(), inputs);
    }
}
