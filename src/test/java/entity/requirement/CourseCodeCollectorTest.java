package entity.requirement;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseCodeCollectorTest {

    private static final CourseCodeCollector COLLECTOR = new CourseCodeCollector();

    private static CourseReq course(String code) {
        return new CourseReq(code, null, false);
    }

    private static List<String> collect(Requirement r) {
        return new ArrayList<>(r.accept(COLLECTOR));
    }

    @Test
    void collectsFromNestedTree() {
        ChooseReq choose = new ChooseReq(1, List.of(course("MAT137Y1"), new NotReq(course("MAT157Y1"), false)), false);
        OrReq or = new OrReq(List.of(course("CSC165H1"), choose), false);
        AndReq r = new AndReq(List.of(course("CSC148H1"), or), null, false);
        assertEquals(List.of("CSC148H1", "CSC165H1", "MAT137Y1", "MAT157Y1"), collect(r));
    }

    @Test
    void collectsFromCreditsFilter() {
        CourseFilter f = CourseFilter.builder().courses(List.of("HPS100H1", "HPS110H1")).build();
        assertEquals(List.of("HPS100H1", "HPS110H1"), collect(new CreditsReq(1.0, null, null, f, false)));
    }

    @Test
    void collectsFromAverageFilterAndItems() {
        CourseFilter f = CourseFilter.builder().courses(List.of("ECO101H1")).build();
        CourseFilter itemFilter = CourseFilter.builder().courses(List.of("ECO102H1")).build();
        AverageReq r = new AverageReq(73, null, false, f, List.of(new CreditsReq(0.5, null, null, itemFilter, false)), false);
        assertEquals(List.of("ECO101H1", "ECO102H1"), collect(r));
    }

    @Test
    void ignoresTextAndProgramNodes() {
        AndReq r = new AndReq(List.of(
                new TextReq(TextKind.PERMISSION, "Permission of the instructor", false),
                new ProgramReq(List.of("ASMAJ1645"), "Drama Major", false),
                course("CSC148H1"),
                new GpaReq(3.0, null, false),
                new RowRefReq(List.of(3), false),
                new ProfileReq("min_year_of_study", 2, "Second year standing", false)), null, false);
        assertEquals(List.of("CSC148H1"), collect(r));
    }

    @Test
    void deduplicates() {
        OrReq r = new OrReq(List.of(course("CSC148H1"),
                new AndReq(List.of(course("CSC148H1"), course("CSC165H1")), null, false)), false);
        assertEquals(List.of("CSC148H1", "CSC165H1"), collect(r));
    }
}
