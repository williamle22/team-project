package entity.plan;

import entity.catalog.Section;
import entity.degree.DegreeType;
import entity.term.Placement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static entity.term.AcademicTerm.fall;
import static entity.term.AcademicTerm.summer;
import static entity.term.AcademicTerm.winter;
import static org.junit.jupiter.api.Assertions.*;

class PlanTest {
    private static final Placement FALL_2026 = Placement.fallWinter(2026, Section.F);
    private static final Placement WINTER_2027 = Placement.fallWinter(2026, Section.S);
    private static final Placement FULL_YEAR_2026 = Placement.fallWinter(2026, Section.Y);
    private static final Placement FALL_2027 = Placement.fallWinter(2027, Section.F);

    private static Plan firstYear() {
        Plan plan = Plan.create("My plan");
        plan.addTerm(fall(2026));
        plan.addTerm(winter(2027));
        return plan;
    }

    @Test
    void createStartsEmpty() {
        Plan plan = Plan.create("My plan");
        assertEquals("My plan", plan.getName());
        assertTrue(plan.getDegree().isEmpty());
        assertTrue(plan.getPrograms().isEmpty());
        assertTrue(plan.getTerms().isEmpty());
        assertTrue(plan.getCourses().isEmpty());
        assertTrue(plan.getTransferCredits().isEmpty());
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> Plan.create(" "));
        assertThrows(IllegalArgumentException.class, () -> firstYear().rename(""));
    }

    @Test
    void addTermKeepsTermsSorted() {
        Plan plan = Plan.create("My plan");
        plan.addTerm(winter(2027));
        plan.addTerm(fall(2026));
        plan.addTerm(summer(2027));
        assertEquals(List.of(fall(2026), winter(2027), summer(2027)), plan.getTerms());
    }

    @Test
    void addTermRejectsDuplicate() {
        assertThrows(IllegalArgumentException.class, () -> firstYear().addTerm(fall(2026)));
    }

    @Test
    void addCourseNeedsItsTerms() {
        Plan plan = Plan.create("My plan");
        plan.addTerm(fall(2026));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> plan.addCourse("MAT137Y1", FULL_YEAR_2026));
        assertEquals("Winter 2027 isn't in this plan", e.getMessage());
        plan.addTerm(winter(2027));
        plan.addCourse("MAT137Y1", FULL_YEAR_2026);
        assertEquals(1, plan.getCourses().size());
    }

    @Test
    void sameCodeSamePlacementThrows() {
        Plan plan = firstYear();
        plan.addCourse("CSC148H1", FALL_2026);
        assertThrows(IllegalArgumentException.class, () -> plan.addCourse("CSC148H1", FALL_2026));
    }

    @Test
    void sameCodeDifferentPlacementIsARetake() {
        Plan plan = firstYear();
        plan.addCourse("CSC148H1", FALL_2026);
        plan.addCourse("CSC148H1", WINTER_2027);
        assertEquals(2, plan.getCourses().size());
    }

    @Test
    void removeTermWithCoursesThrows() {
        Plan plan = firstYear();
        plan.addCourse("MAT137Y1", FULL_YEAR_2026);
        assertThrows(IllegalStateException.class, () -> plan.removeTerm(winter(2027)));
    }

    @Test
    void removeTermAfterMovingCourse() {
        Plan plan = firstYear();
        PlannedCourse c = plan.addCourse("CSC148H1", WINTER_2027);
        plan.moveCourse(c.getId(), FALL_2026);
        plan.removeTerm(winter(2027));
        assertEquals(List.of(fall(2026)), plan.getTerms());
        assertEquals(FALL_2026, plan.findCourse(c.getId()).get().getPlacement());
    }

    @Test
    void removeTermRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> firstYear().removeTerm(fall(2030)));
    }

    @Test
    void moveCourseChecksTheRules() {
        Plan plan = firstYear();
        PlannedCourse first = plan.addCourse("CSC148H1", FALL_2026);
        PlannedCourse retake = plan.addCourse("CSC148H1", WINTER_2027);
        assertThrows(IllegalArgumentException.class, () -> plan.moveCourse(first.getId(), FALL_2027));
        assertThrows(IllegalArgumentException.class, () -> plan.moveCourse(retake.getId(), FALL_2026));
        plan.moveCourse(first.getId(), FALL_2026);
    }

    @Test
    void coursesInIncludesFullYearInWinter() {
        Plan plan = firstYear();
        PlannedCourse mat = plan.addCourse("MAT137Y1", FULL_YEAR_2026);
        plan.addCourse("CSC148H1", FALL_2026);
        PlannedCourse csc = plan.addCourse("CSC165H1", WINTER_2027);
        assertEquals(List.of(mat, csc), plan.coursesIn(winter(2027)));
    }

    @Test
    void coursesEndedBefore() {
        Plan plan = firstYear();
        plan.addCourse("MAT137Y1", FULL_YEAR_2026);
        PlannedCourse csc148 = plan.addCourse("CSC148H1", FALL_2026);
        plan.addCourse("CSC165H1", WINTER_2027);
        assertEquals(List.of(csc148), plan.coursesEndedBefore(winter(2027)));
        assertTrue(plan.coursesEndedBefore(fall(2026)).isEmpty());
    }

    @Test
    void coursesInAcademicYearIncludesSummer() {
        Plan plan = firstYear();
        plan.addTerm(summer(2027));
        plan.addTerm(fall(2027));
        PlannedCourse fall = plan.addCourse("CSC148H1", FALL_2026);
        PlannedCourse summer = plan.addCourse("CSC165H1", Placement.summer(2027, Section.F));
        plan.addCourse("CSC207H1", FALL_2027);
        assertEquals(List.of(fall, summer), plan.coursesInAcademicYear(2026));
    }

    @Test
    void gettersAreReadOnly() {
        Plan plan = firstYear();
        assertThrows(UnsupportedOperationException.class, () -> plan.getPrograms().clear());
        assertThrows(UnsupportedOperationException.class, () -> plan.getTerms().clear());
        assertThrows(UnsupportedOperationException.class, () -> plan.getCourses().clear());
        assertThrows(UnsupportedOperationException.class, () -> plan.getTransferCredits().clear());
    }

    @Test
    void replaceCourseSwapsById() {
        Plan plan = firstYear();
        PlannedCourse c = plan.addCourse("CSC148H1", FALL_2026);
        PlannedCourse graded = c.withCrNcr(true);
        plan.replaceCourse(graded);
        assertEquals(List.of(graded), plan.getCourses());
    }

    @Test
    void unknownCourseIdThrows() {
        Plan plan = firstYear();
        UUID missing = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,
                () -> plan.replaceCourse(PlannedCourse.create("CSC148H1", FALL_2026)));
        assertThrows(IllegalArgumentException.class, () -> plan.moveCourse(missing, FALL_2026));
        assertThrows(IllegalArgumentException.class, () -> plan.removeCourse(missing));
    }

    @Test
    void removeCourse() {
        Plan plan = firstYear();
        PlannedCourse c = plan.addCourse("CSC148H1", FALL_2026);
        plan.removeCourse(c.getId());
        assertTrue(plan.getCourses().isEmpty());
    }

    @Test
    void programStatusLists() {
        Plan plan = firstYear();
        plan.addProgram("ASMAJ1689", ProgramStatus.INTENDED);
        plan.addProgram("ASMIN1001", ProgramStatus.ENROLLED);
        plan.addProgram("ASMAJ0110", ProgramStatus.INTENDED);
        assertEquals(List.of("ASMIN1001"), plan.enrolledPrograms());
        assertEquals(List.of("ASMAJ1689", "ASMAJ0110"), plan.intendedPrograms());
        plan.setProgramStatus("ASMAJ1689", ProgramStatus.ENROLLED);
        assertEquals(List.of("ASMAJ1689", "ASMIN1001"), plan.enrolledPrograms());
    }

    @Test
    void programRules() {
        Plan plan = firstYear();
        plan.addProgram("ASMAJ1689", ProgramStatus.INTENDED);
        assertThrows(IllegalArgumentException.class, () -> plan.addProgram("ASMAJ1689", ProgramStatus.ENROLLED));
        assertThrows(IllegalArgumentException.class, () -> plan.setProgramStatus("ASMIN1001", ProgramStatus.ENROLLED));
        assertThrows(IllegalArgumentException.class, () -> plan.removeProgram("ASMIN1001"));
        plan.removeProgram("ASMAJ1689");
        assertTrue(plan.getPrograms().isEmpty());
    }

    @Test
    void transferCreditRules() {
        Plan plan = firstYear();
        plan.addTransferCredit(new TransferCredit("MAT135H1", 0.5));
        assertThrows(IllegalArgumentException.class, () -> plan.addTransferCredit(new TransferCredit("MAT135H1", 0.5)));
        assertThrows(IllegalArgumentException.class, () -> plan.removeTransferCredit("MAT136H1"));
        plan.removeTransferCredit("MAT135H1");
        assertTrue(plan.getTransferCredits().isEmpty());
    }

    @Test
    void copyIsEqualButIndependent() {
        Plan plan = firstYear();
        plan.addCourse("CSC148H1", FALL_2026);
        Plan copy = plan.copy();
        assertEquals(plan, copy);
        plan.addCourse("CSC165H1", WINTER_2027);
        plan.rename("Changed");
        assertEquals(1, copy.getCourses().size());
        assertEquals("My plan", copy.getName());
    }

    @Test
    void copyWithNewIdChangesOnlyTheId() {
        Plan plan = firstYear();
        plan.setDegree(DegreeType.HBSC);
        plan.addProgram("ASMAJ1689", ProgramStatus.ENROLLED);
        plan.addCourse("CSC148H1", FALL_2026);
        plan.addTransferCredit(new TransferCredit("MAT135H1", 0.5));
        Plan copy = plan.copyWithNewId();
        assertNotEquals(plan.getId(), copy.getId());
        assertEquals(plan.getName(), copy.getName());
        assertEquals(plan.getDegree(), copy.getDegree());
        assertEquals(plan.getPrograms(), copy.getPrograms());
        assertEquals(plan.getTerms(), copy.getTerms());
        assertEquals(plan.getCourses(), copy.getCourses());
        assertEquals(plan.getTransferCredits(), copy.getTransferCredits());
    }

    @Test
    void restoreRejectsCourseOutsideTerms() {
        PlannedCourse c = PlannedCourse.create("CSC148H1", FALL_2027);
        assertThrows(IllegalArgumentException.class, () -> Plan.restore(UUID.randomUUID(), "My plan", null,
                List.of(), List.of(fall(2026), winter(2027)), List.of(c), List.of()));
    }

    @Test
    void restoreRejectsDuplicateCourseId() {
        PlannedCourse c = PlannedCourse.create("CSC148H1", FALL_2026);
        assertThrows(IllegalArgumentException.class, () -> Plan.restore(UUID.randomUUID(), "My plan", null,
                List.of(), List.of(fall(2026), winter(2027)), List.of(c, c.withPlacement(WINTER_2027)), List.of()));
    }
}
