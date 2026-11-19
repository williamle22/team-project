package use_case.search_courses;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SearchCoursesInputDataTest {
    private static final SearchFilters CSC = new SearchFilters("CSC", Set.of(), Set.of(), Set.of());

    @Test
    void gettersReturnConstructorValues() {
        SearchCoursesInputData input = new SearchCoursesInputData(" csc 148 ", CSC);
        assertEquals(" csc 148 ", input.getQuery());
        assertEquals(CSC, input.getFilters());
    }

    @Test
    void rejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new SearchCoursesInputData(null, CSC));
        assertThrows(NullPointerException.class, () -> new SearchCoursesInputData("csc", null));
    }

    @Test
    void toStringShowsQuery() {
        assertTrue(new SearchCoursesInputData("intro computer", CSC).toString().contains("intro computer"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(SearchCoursesInputData.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
