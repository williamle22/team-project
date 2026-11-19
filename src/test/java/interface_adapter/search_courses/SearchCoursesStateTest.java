package interface_adapter.search_courses;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchCoursesStateTest {
    private static final SearchResultRow ROW = new SearchResultRow("CSC148H1", "t", "0.5", "UTSG");

    @Test
    void defaultsToEmpty() {
        SearchCoursesState s = new SearchCoursesState();
        assertEquals(List.of(), s.getResults());
        assertEquals("", s.getMessage());
        assertEquals("", s.getFooter());
    }

    @Test
    void settersUpdateFields() {
        SearchCoursesState s = new SearchCoursesState();
        s.setResults(List.of(ROW));
        s.setMessage("");
        s.setFooter("Showing the first 100 of 412");
        assertEquals(List.of(ROW), s.getResults());
        assertEquals("", s.getMessage());
        assertEquals("Showing the first 100 of 412", s.getFooter());
    }

    @Test
    void resultsAreCopied() {
        List<SearchResultRow> rows = new ArrayList<>(List.of(ROW));
        SearchCoursesState s = new SearchCoursesState();
        s.setResults(rows);
        rows.clear();
        assertEquals(List.of(ROW), s.getResults());
        assertThrows(UnsupportedOperationException.class, () -> s.getResults().add(ROW));
    }

    @Test
    void toStringShowsCountAndMessage() {
        SearchCoursesState s = new SearchCoursesState();
        s.setResults(List.of(ROW));
        s.setMessage("No courses match.");
        assertTrue(s.toString().contains("1 results"));
        assertTrue(s.toString().contains("No courses match."));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(SearchCoursesState.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
