package data_access.catalog;

import entity.catalog.CatalogVersion;
import entity.catalog.ProgramType;
import org.junit.jupiter.api.Test;
import use_case.search_courses.SearchFilters;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static testutil.CatalogFixtures.course;
import static testutil.CatalogFixtures.program;

class LiveCatalogRepositoryTest {
    private static final CatalogVersion VERSION = new CatalogVersion("2026-2027", Instant.parse("2026-11-12T10:00:04Z"));

    @Test
    void emptyUntilReplaced() {
        LiveCatalogRepository live = new LiveCatalogRepository();
        assertFalse(live.isLoaded());
        assertEquals(Optional.empty(), live.version());
        assertTrue(live.allCourses().isEmpty());
        assertTrue(live.allPrograms().isEmpty());
        assertEquals(Optional.empty(), live.findCourse("CSC148H1"));
        assertEquals(Optional.empty(), live.findProgram("ASMIN1001"));
    }

    @Test
    void delegatesAfterReplace() {
        InMemoryCatalogRepository catalog = new InMemoryCatalogRepository(List.of(course("CSC148H1")),
                List.of(program("ASMIN1001", ProgramType.MINOR, null)), VERSION);
        LiveCatalogRepository live = new LiveCatalogRepository();
        live.replace(catalog);

        assertTrue(live.isLoaded());
        assertEquals(Optional.of(VERSION), live.version());
        assertIterableEquals(catalog.allCourses(), live.allCourses());
        assertIterableEquals(catalog.allPrograms(), live.allPrograms());
        assertEquals(catalog.findCourse("csc 148h1"), live.findCourse("csc 148h1"));
        assertEquals(catalog.findProgram("ASMIN1001"), live.findProgram("ASMIN1001"));
    }

    @Test
    void replaceRejectsNull() {
        assertThrows(NullPointerException.class, () -> new LiveCatalogRepository().replace(null));
    }

    @Test
    void searchUsesNewCatalogAfterReplace() {
        LiveCatalogRepository live = new LiveCatalogRepository();
        assertEquals(List.of(), live.search("", SearchFilters.none()));

        live.replace(new InMemoryCatalogRepository(List.of(course("CSC148H1")), List.of(), VERSION));
        assertEquals(List.of(course("CSC148H1")), live.search("csc148", SearchFilters.none()));

        live.replace(new InMemoryCatalogRepository(List.of(course("MAT137Y1")), List.of(), VERSION));
        assertEquals(List.of(), live.search("csc148", SearchFilters.none()));
        assertEquals(List.of(course("MAT137Y1")), live.search("mat", SearchFilters.none()));
    }
}
