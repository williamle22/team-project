package data_access.catalog;

import entity.catalog.CatalogVersion;
import entity.catalog.Course;
import entity.catalog.ProgramType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static testutil.CatalogFixtures.course;
import static testutil.CatalogFixtures.program;

class InMemoryCatalogRepositoryTest {

    private static final CatalogVersion VERSION = new CatalogVersion("2026-2027", Instant.parse("2026-11-05T18:34:42Z"));

    private static InMemoryCatalogRepository repo() {
        return new InMemoryCatalogRepository(
                List.of(course("CSC207H1"), course("CSC148H1"), course("MAT137Y1")),
                List.of(program("ASMIN1001", ProgramType.MINOR, null)),
                VERSION);
    }

    @Test
    void findsByCode() {
        assertEquals("CSC148H1", repo().findCourse("CSC148H1").orElseThrow().getCode());
        assertEquals("ASMIN1001", repo().findProgram("ASMIN1001").orElseThrow().getCode());
    }

    @Test
    void findIgnoresCaseAndSpaces() {
        assertTrue(repo().findCourse("csc 148h1").isPresent());
        assertTrue(repo().findProgram("asmin 1001").isPresent());
    }

    @Test
    void missingCodeIsEmpty() {
        assertTrue(repo().findCourse("CSC999H1").isEmpty());
        assertTrue(repo().findProgram("ASMIN9999").isEmpty());
    }

    @Test
    void duplicateCodeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new InMemoryCatalogRepository(
                List.of(course("CSC148H1"), course("CSC148H1")), List.of(), VERSION));
        assertThrows(IllegalArgumentException.class, () -> new InMemoryCatalogRepository(List.of(),
                List.of(program("ASMIN1001", ProgramType.MINOR, null), program("ASMIN1001", ProgramType.MINOR, null)),
                VERSION));
    }

    @Test
    void allCoursesSortedByCode() {
        List<String> codes = new ArrayList<>();
        for (Course c : repo().allCourses()) {
            codes.add(c.getCode());
        }
        assertEquals(List.of("CSC148H1", "CSC207H1", "MAT137Y1"), codes);
    }

    @Test
    void loadedHasVersion() {
        assertTrue(repo().isLoaded());
        assertEquals(VERSION, repo().version().orElseThrow());
    }

    @Test
    void emptyIsNotLoaded() {
        InMemoryCatalogRepository empty = InMemoryCatalogRepository.empty();
        assertFalse(empty.isLoaded());
        assertTrue(empty.version().isEmpty());
        assertTrue(empty.allCourses().isEmpty());
    }
}
