package data_access.catalog;

import entity.catalog.Campus;
import entity.catalog.Course;
import org.junit.jupiter.api.Test;
import use_case.search_courses.SearchFilters;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CourseSearchIndexTest {
    private static final Course CSC108 = course("CSC108H1", "Introduction to Computer Programming");
    private static final Course CSC148 = course("CSC148H1", "Introduction to Computer Science");
    private static final Course CSC369 = course("CSC369H1", "Operating Systems");
    private static final Course MAT137 = course("MAT137Y1", "Calculus with Proofs");
    private static final Course STA130 =
            course("STA130H1", "An Introduction to Statistical Reasoning and Data Science");

    private static final CourseSearchIndex INDEX =
            new CourseSearchIndex(List.of(CSC108, CSC148, CSC369, MAT137, STA130));

    private static Course course(String code, String title) {
        int level = (code.charAt(3) - '0') * 100;
        double weight = code.charAt(6) == 'Y' ? 1.0 : 0.5;
        return new Course(code, code.substring(0, 3), code.substring(3, 6), level, weight, Campus.UTSG, title, "",
                null, null, null, List.of(), null);
    }

    private static List<Course> search(String query) {
        return INDEX.search(query, SearchFilters.none());
    }

    private static SearchFilters levels(Integer... levels) {
        return new SearchFilters(null, Set.of(levels), Set.of(), Set.of());
    }

    @Test
    void byCodePrefix() {
        assertEquals(List.of(CSC108, CSC148), search("CSC1"));
        assertEquals(List.of(CSC148), search("csc148"));
        assertEquals(List.of(CSC148), search("CSC148H1"));
    }

    @Test
    void byCodeWithSpace() {
        assertEquals(List.of(CSC148), search("csc 148"));
        assertEquals(List.of(CSC148), search("CSC  148 h1"));
    }

    @Test
    void byCodeNumber() {
        assertEquals(List.of(MAT137), search("137"));
    }

    @Test
    void byTitleWords() {
        assertEquals(List.of(CSC108, CSC148), search("intro computer"));
        assertEquals(List.of(CSC369), search("OPERATING"));
    }

    @Test
    void wordsInAnyOrder() {
        assertEquals(List.of(CSC148), search("science computer"));
        assertEquals(List.of(CSC148, STA130), search("science intro"));
    }

    @Test
    void codeAndTitleWordsTogether() {
        assertEquals(List.of(CSC108, CSC148), search("csc introduction"));
    }

    @Test
    void ignoresSurroundingSpaces() {
        assertEquals(List.of(CSC369), search("  csc369  "));
        assertEquals(List.of(CSC369), search(" operating systems "));
    }

    @Test
    void noMatchIsEmpty() {
        assertEquals(List.of(), search("quantum basket weaving"));
    }

    @Test
    void blankQueryMatchesAll() {
        assertEquals(List.of(CSC108, CSC148, CSC369, MAT137, STA130), search(""));
        assertEquals(List.of(CSC108, CSC148, CSC369, MAT137, STA130), search("   "));
    }

    @Test
    void blankQueryWithFilter() {
        assertEquals(List.of(CSC369), INDEX.search("", levels(300)));
    }

    @Test
    void filtersNarrow() {
        SearchFilters csc = new SearchFilters("csc", Set.of(), Set.of(), Set.of());
        assertEquals(List.of(CSC108, CSC148), INDEX.search("intro", csc));
        assertEquals(List.of(CSC108, CSC148, STA130), INDEX.search("intro", levels(100)));
        assertEquals(List.of(), INDEX.search("intro", levels(400)));
    }

    @Test
    void keepsInputOrder() {
        CourseSearchIndex reversed = new CourseSearchIndex(List.of(STA130, CSC148, CSC108));
        assertEquals(List.of(STA130, CSC148, CSC108), reversed.search("intro", SearchFilters.none()));
    }

    @Test
    void emptyIndexFindsNothing() {
        assertEquals(List.of(), CourseSearchIndex.empty().search("", SearchFilters.none()));
        assertEquals(List.of(), CourseSearchIndex.empty().search("csc148", SearchFilters.none()));
    }
}
