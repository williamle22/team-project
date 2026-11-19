package use_case.search_courses;

import entity.catalog.Campus;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchCoursesOutputDataTest {
    private static final SearchResultItem CSC148 =
            new SearchResultItem("CSC148H1", "Introduction to Computer Science", 0.5, Campus.UTSG, List.of(5), true);

    @Test
    void gettersReturnConstructorValues() {
        SearchCoursesOutputData data = new SearchCoursesOutputData(List.of(CSC148), 1);
        assertEquals(List.of(CSC148), data.getResults());
        assertEquals(1, data.getTotalMatches());
    }

    @Test
    void truncatedWhenMoreMatchesThanResults() {
        assertTrue(new SearchCoursesOutputData(List.of(CSC148), 412).isTruncated());
        assertFalse(new SearchCoursesOutputData(List.of(CSC148), 1).isTruncated());
        assertFalse(new SearchCoursesOutputData(List.of(), 0).isTruncated());
    }

    @Test
    void resultsAreCopied() {
        List<SearchResultItem> results = new ArrayList<>(List.of(CSC148));
        SearchCoursesOutputData data = new SearchCoursesOutputData(results, 1);
        results.clear();
        assertEquals(List.of(CSC148), data.getResults());
        assertThrows(UnsupportedOperationException.class, () -> data.getResults().add(CSC148));
    }

    @Test
    void rejectsNullResults() {
        assertThrows(NullPointerException.class, () -> new SearchCoursesOutputData(null, 0));
    }

    @Test
    void toStringShowsCounts() {
        String s = new SearchCoursesOutputData(List.of(CSC148), 412).toString();
        assertTrue(s.contains("1 of 412"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(SearchCoursesOutputData.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
