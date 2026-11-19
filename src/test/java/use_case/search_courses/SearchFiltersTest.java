package use_case.search_courses;

import entity.catalog.BreadthCategory;
import entity.catalog.Campus;
import entity.catalog.Course;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static entity.catalog.BreadthCategory.LIVING_THINGS;
import static entity.catalog.BreadthCategory.PHYSICAL_MATHEMATICAL;
import static entity.catalog.BreadthCategory.THOUGHT_BELIEF;
import static org.junit.jupiter.api.Assertions.*;
import static testutil.CatalogFixtures.course;
import static testutil.CatalogFixtures.withBreadth;

class SearchFiltersTest {
    private static final Course CSC369 = withBreadth("CSC369H1", PHYSICAL_MATHEMATICAL);

    private static SearchFilters filters(String designator, Set<Integer> levels, Set<Campus> campuses,
                                         Set<BreadthCategory> breadth) {
        return new SearchFilters(designator, levels, campuses, breadth);
    }

    private static Course online(String code) {
        return new Course(code, code.substring(0, 3), code.substring(3, 6), 100, 0.5, Campus.ONLINE,
                code + " title", "", null, null, null, List.of(), null);
    }

    @Test
    void noneHasNoFilters() {
        SearchFilters none = SearchFilters.none();
        assertEquals(Optional.empty(), none.getDesignator());
        assertEquals(Set.of(), none.getLevels());
        assertEquals(Set.of(), none.getCampuses());
        assertEquals(Set.of(), none.getBreadth());
    }

    @Test
    void emptyAcceptsAll() {
        assertTrue(SearchFilters.none().accepts(CSC369));
        assertTrue(SearchFilters.none().accepts(course("MAT137Y1")));
        assertTrue(SearchFilters.none().accepts(online("CSC108H1")));
    }

    @Test
    void blankDesignatorMeansAny() {
        assertEquals(Optional.empty(), filters("  ", Set.of(), Set.of(), Set.of()).getDesignator());
        assertTrue(filters("", Set.of(), Set.of(), Set.of()).accepts(CSC369));
    }

    @Test
    void designatorIsCaseInsensitive() {
        SearchFilters csc = filters(" csc ", Set.of(), Set.of(), Set.of());
        assertEquals(Optional.of("CSC"), csc.getDesignator());
        assertTrue(csc.accepts(CSC369));
        assertFalse(csc.accepts(course("MAT137Y1")));
    }

    @Test
    void levelFilter() {
        SearchFilters thirdYear = filters(null, Set.of(300), Set.of(), Set.of());
        assertTrue(thirdYear.accepts(CSC369));
        assertFalse(thirdYear.accepts(course("CSC148H1")));
        assertTrue(filters(null, Set.of(100, 300), Set.of(), Set.of()).accepts(course("CSC148H1")));
    }

    @Test
    void campusFilter() {
        SearchFilters onlineOnly = filters(null, Set.of(), Set.of(Campus.ONLINE), Set.of());
        assertTrue(onlineOnly.accepts(online("CSC108H1")));
        assertFalse(onlineOnly.accepts(CSC369));
    }

    @Test
    void breadthMatchesAnyShared() {
        Course twoCategories = withBreadth("EEB202H1", THOUGHT_BELIEF, LIVING_THINGS);
        assertTrue(filters(null, Set.of(), Set.of(), Set.of(LIVING_THINGS)).accepts(twoCategories));
        assertTrue(filters(null, Set.of(), Set.of(), Set.of(LIVING_THINGS, PHYSICAL_MATHEMATICAL))
                .accepts(CSC369));
        assertFalse(filters(null, Set.of(), Set.of(), Set.of(PHYSICAL_MATHEMATICAL)).accepts(twoCategories));
        assertFalse(filters(null, Set.of(), Set.of(), Set.of(LIVING_THINGS)).accepts(course("CSC148H1")));
    }

    @Test
    void allFiltersMatch() {
        assertTrue(filters("CSC", Set.of(300), Set.of(Campus.UTSG), Set.of(PHYSICAL_MATHEMATICAL)).accepts(CSC369));
    }

    @Test
    void eachFilterCanReject() {
        Set<Integer> l = Set.of(300);
        Set<Campus> c = Set.of(Campus.UTSG);
        Set<BreadthCategory> b = Set.of(PHYSICAL_MATHEMATICAL);
        assertFalse(filters("MAT", l, c, b).accepts(CSC369));
        assertFalse(filters("CSC", Set.of(400), c, b).accepts(CSC369));
        assertFalse(filters("CSC", l, Set.of(Campus.ONLINE), b).accepts(CSC369));
        assertFalse(filters("CSC", l, c, Set.of(LIVING_THINGS)).accepts(CSC369));
    }

    @Test
    void setsAreCopied() {
        Set<Integer> levels = new HashSet<>(Set.of(300));
        SearchFilters f = filters(null, levels, Set.of(), Set.of());
        levels.add(100);
        assertEquals(Set.of(300), f.getLevels());
        assertThrows(UnsupportedOperationException.class, () -> f.getLevels().add(200));
    }

    @Test
    void rejectsNullSets() {
        assertThrows(NullPointerException.class, () -> filters(null, null, Set.of(), Set.of()));
        assertThrows(NullPointerException.class, () -> filters(null, Set.of(), null, Set.of()));
        assertThrows(NullPointerException.class, () -> filters(null, Set.of(), Set.of(), null));
    }

    @Test
    void toStringShowsFilters() {
        String s = filters("csc", Set.of(300), Set.of(), Set.of()).toString();
        assertTrue(s.contains("CSC"));
        assertTrue(s.contains("300"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(SearchFilters.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
