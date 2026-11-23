package interface_adapter.search_programs;

import org.junit.jupiter.api.Test;
import use_case.search_programs.SearchProgramsInputData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static entity.catalog.ProgramType.MAJOR;
import static entity.catalog.ProgramType.MINOR;
import static org.junit.jupiter.api.Assertions.*;

class SearchProgramsControllerTest {
    private final List<SearchProgramsInputData> inputs = new ArrayList<>();
    private final SearchProgramsController controller = new SearchProgramsController(inputs::add);

    @Test
    void convertsTypeLabels() {
        controller.execute(" computer science ", Set.of("Major", "Minor"));
        assertEquals(List.of(new SearchProgramsInputData(" computer science ", Set.of(MAJOR, MINOR))), inputs);
    }

    @Test
    void emptyTypesMeanAny() {
        controller.execute("", Set.of());
        assertEquals(List.of(new SearchProgramsInputData("", Set.of())), inputs);
    }

    @Test
    void unknownTypeThrows() {
        assertThrows(IllegalArgumentException.class, () -> controller.execute("", Set.of("major")));
        assertEquals(List.of(), inputs);
    }
}
