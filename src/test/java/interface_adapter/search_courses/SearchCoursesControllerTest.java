package interface_adapter.search_courses;

import entity.catalog.BreadthCategory;
import entity.catalog.Campus;
import org.junit.jupiter.api.Test;
import use_case.search_courses.SearchCoursesInputData;
import use_case.search_courses.SearchFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SearchCoursesControllerTest {
    private final List<SearchCoursesInputData> inputs = new ArrayList<>();
    private final SearchCoursesController controller = new SearchCoursesController(inputs::add);

    @Test
    void convertsWidgetValuesToFilters() {
        controller.execute(" csc 148 ", "csc", Set.of(100, 300), Set.of("utsg", "online"), Set.of(4, 5));
        SearchFilters expected = new SearchFilters("CSC", Set.of(100, 300), Set.of(Campus.UTSG, Campus.ONLINE),
                Set.of(BreadthCategory.LIVING_THINGS, BreadthCategory.PHYSICAL_MATHEMATICAL));
        assertEquals(List.of(new SearchCoursesInputData(" csc 148 ", expected)), inputs);
    }

    @Test
    void emptyWidgetsMeanNoFilters() {
        controller.execute("", "", Set.of(), Set.of(), Set.of());
        assertEquals(List.of(new SearchCoursesInputData("", SearchFilters.none())), inputs);
    }

    @Test
    void unknownCampusThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.execute("", null, Set.of(), Set.of("UTSG"), Set.of()));
        assertEquals(List.of(), inputs);
    }
}
