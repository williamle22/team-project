package use_case;

import entity.catalog.Course;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static testutil.CatalogFixtures.course;

class CodeRankingTest {
    @Test
    void ordersByRankThenCode() {
        List<Course> courses = new ArrayList<>(List.of(course("CSC148H1"), course("MAT148H1"), course("CSC108H1"),
                course("BIO130H1")));
        courses.sort(CodeRanking.by("CSC1", Course::getCode));
        assertEquals(List.of(course("CSC108H1"), course("CSC148H1"), course("BIO130H1"), course("MAT148H1")),
                courses);
    }

    @Test
    void exactMatchIgnoresSpacesAndCase() {
        List<Course> courses = new ArrayList<>(List.of(course("BIO130H1"), course("CSC148H1"), course("CSC148Y1")));
        courses.sort(CodeRanking.by("csc 148h1", Course::getCode));
        assertEquals(List.of(course("CSC148H1"), course("BIO130H1"), course("CSC148Y1")), courses);
    }
}
