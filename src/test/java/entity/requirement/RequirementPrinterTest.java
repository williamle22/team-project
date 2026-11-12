package entity.requirement;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequirementPrinterTest {

    private static final RequirementPrinter PRINTER = new RequirementPrinter();

    private static CourseReq course(String code) {
        return new CourseReq(code, null, false);
    }

    @Test
    void printsPhy152Prerequisite() {
        OrReq r = new OrReq(List.of(course("PHY131H1"), course("PHY151H1")), false);
        assertEquals("(PHY131H1 or PHY151H1)", PRINTER.print(r));
    }

    @Test
    void printsAnd() {
        AndReq r = new AndReq(List.of(course("CSC148H1"), course("CSC165H1")), null, false);
        assertEquals("(CSC148H1 and CSC165H1)", PRINTER.print(r));
    }

    @Test
    void printsChoose() {
        ChooseReq r = new ChooseReq(2, List.of(course("MAT137Y1"), course("MAT157Y1"), course("MAT135H1")), false);
        assertEquals("2 of (MAT137Y1, MAT157Y1, MAT135H1)", PRINTER.print(r));
    }

    @Test
    void printsNot() {
        assertEquals("not CSC207H1", PRINTER.print(new NotReq(course("CSC207H1"), false)));
    }

    @Test
    void printsCourse() {
        assertEquals("CSC148H1", PRINTER.print(course("CSC148H1")));
        assertEquals("CSC148H1 (min. 70%)", PRINTER.print(new CourseReq("CSC148H1", 70, false)));
    }

    @Test
    void printsCredits() {
        CourseFilter f = CourseFilter.builder().designators(List.of("HPS")).levels(List.of(300, 400)).build();
        assertEquals("1.0 credits in HPS at the 300/400 level", PRINTER.print(new CreditsReq(1.0, null, null, f, false)));
    }

    @Test
    void printsCreditsMaximum() {
        CourseFilter f = CourseFilter.builder().levels(List.of(100)).build();
        assertEquals("at most 1.0 credits at the 100 level", PRINTER.print(new CreditsReq(0, 1.0, null, f, false)));
    }

    @Test
    void printsEveryFilterField() {
        CourseFilter f = CourseFilter.builder()
                .courses(List.of("HPS100H1", "HPS110H1"))
                .designators(List.of("HPS", "PHL"))
                .levels(List.of(300, 400))
                .breadth(List.of(4, 5))
                .lists(List.of("Group A", "Group B"))
                .exclude(List.of("HPS390H1"))
                .minGrade(70)
                .build();
        assertEquals("2.0 credits from HPS100H1, HPS110H1 in HPS/PHL at the 300/400 level in breadth 4/5"
                        + " from Group A, Group B excluding HPS390H1 with at least 70%",
                PRINTER.print(new CreditsReq(2.0, null, null, f, false)));
    }

    @Test
    void printsAverage() {
        CourseFilter f = CourseFilter.builder().designators(List.of("ECO")).build();
        assertEquals("an average of at least 73% in ECO over 3.0 credits",
                PRINTER.print(new AverageReq(73, 3.0, false, f, List.of(), false)));
        assertEquals("an average of at least 73% in ECO",
                PRINTER.print(new AverageReq(73, null, false, f, List.of(), false)));
    }

    @Test
    void printsGpa() {
        assertEquals("a GPA of at least 3.00 (CHM credits)", PRINTER.print(new GpaReq(3.0, "CHM credits", false)));
        assertEquals("a GPA of at least 2.50", PRINTER.print(new GpaReq(2.5, null, false)));
    }

    @Test
    void printsTextOfProgramProfileAndText() {
        assertEquals("Drama Major, Specialist or Minor",
                PRINTER.print(new ProgramReq(List.of("ASMAJ1645"), "Drama Major, Specialist or Minor", false)));
        assertEquals("Second year standing",
                PRINTER.print(new ProfileReq("min_year_of_study", 2, "Second year standing", false)));
        assertEquals("Permission of the instructor",
                PRINTER.print(new TextReq(TextKind.PERMISSION, "Permission of the instructor", false)));
    }

    @Test
    void printsRowRef() {
        assertEquals("Requirement 3", PRINTER.print(new RowRefReq(List.of(3), false)));
        assertEquals("Requirements 3 or 4", PRINTER.print(new RowRefReq(List.of(3, 4), false)));
        assertEquals("Requirements 2, 3 or 4", PRINTER.print(new RowRefReq(List.of(2, 3, 4), false)));
    }

    @Test
    void marksConcurrentNodes() {
        assertEquals("CSC148H1 (may be taken concurrently)", PRINTER.print(new CourseReq("CSC148H1", null, true)));
        OrReq r = new OrReq(List.of(course("PHY131H1"), new CourseReq("PHY151H1", null, true)), false);
        assertEquals("(PHY131H1 or PHY151H1 (may be taken concurrently))", PRINTER.print(r));
    }

    @Test
    void printsNestedTree() {
        OrReq or = new OrReq(List.of(course("MAT135H1"), course("MAT137Y1")), false);
        AndReq r = new AndReq(List.of(course("CSC148H1"), or), null, false);
        assertEquals("(CSC148H1 and (MAT135H1 or MAT137Y1))", PRINTER.print(r));
    }

    @Test
    void printsAmountWhenMinimumAndMaximumBothSet() {
        CourseFilter f = CourseFilter.builder().designators(List.of("HPS")).build();
        assertEquals("1.0 credits in HPS", PRINTER.print(new CreditsReq(1.0, 2.0, null, f, false)));
    }
}
