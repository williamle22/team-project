package entity.plan;

import entity.catalog.Section;
import entity.grades.Assessment;
import entity.grades.CourseGrade;
import entity.term.Placement;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.List;

import static entity.term.AcademicTerm.fall;
import static entity.term.AcademicTerm.winter;
import static org.junit.jupiter.api.Assertions.*;

class GpaCalculatorTest {
    private static final double DELTA = 1e-9;
    private static final Placement FALL_2026 = Placement.fallWinter(2026, Section.F);
    private static final Placement WINTER_2027 = Placement.fallWinter(2026, Section.S);
    private static final Placement FULL_YEAR_2026 = Placement.fallWinter(2026, Section.Y);
    private static final Placement FALL_2027 = Placement.fallWinter(2027, Section.F);
    private static final Placement WINTER_2028 = Placement.fallWinter(2027, Section.S);
    private static final Placement FULL_YEAR_2027 = Placement.fallWinter(2027, Section.Y);

    private final GpaCalculator calculator = new GpaCalculator(CreditWeights.FROM_CODE);

    private static Plan twoYears() {
        Plan plan = Plan.create("My plan");
        plan.addTerm(fall(2026));
        plan.addTerm(winter(2027));
        plan.addTerm(fall(2027));
        return plan;
    }

    private static PlannedCourse graded(Plan plan, String code, Placement placement, int finalGrade) {
        PlannedCourse c = plan.addCourse(code, placement);
        PlannedCourse withGrade = c.withGrade(CourseGrade.empty().withFinalGrade(finalGrade));
        plan.replaceCourse(withGrade);
        return withGrade;
    }

    @Test
    void noGradesIsEmpty() {
        Plan plan = twoYears();
        plan.addCourse("CSC148H1", FALL_2026);
        assertTrue(calculator.cgpa(plan).isEmpty());
        assertTrue(calculator.sgpa(plan, fall(2026)).isEmpty());
        assertTrue(calculator.projectedCgpa(plan).isEmpty());
    }

    @Test
    void weightsByCredit() {
        Plan plan = twoYears();
        graded(plan, "MAT137Y1", FULL_YEAR_2026, 92);
        graded(plan, "CSC148H1", FALL_2026, 64);
        assertEquals((4.0 * 1.0 + 2.0 * 0.5) / 1.5, calculator.cgpa(plan).getAsDouble(), DELTA);
    }

    @Test
    void crNcrExcluded() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 90);
        PlannedCourse crNcr = graded(plan, "PSY100H1", FALL_2026, 55);
        plan.replaceCourse(crNcr.withCrNcr(true));
        assertEquals(4.0, calculator.cgpa(plan).getAsDouble(), DELTA);
    }

    @Test
    void transferCreditsExcluded() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 90);
        plan.addTransferCredit(new TransferCredit("MAT135H1", 0.5));
        assertEquals(4.0, calculator.cgpa(plan).getAsDouble(), DELTA);
    }

    @Test
    void failingCountsAsZero() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 90);
        graded(plan, "CSC165H1", FALL_2026, 30);
        assertEquals(2.0, calculator.cgpa(plan).getAsDouble(), DELTA);
    }

    @Test
    void cgpaBeforeIgnoresLaterTerms() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 90);
        graded(plan, "CSC165H1", WINTER_2027, 64);
        assertEquals(4.0, calculator.cgpaBefore(plan, winter(2027)).getAsDouble(), DELTA);
        assertEquals(3.0, calculator.cgpaBefore(plan, fall(2027)).getAsDouble(), DELTA);
        assertTrue(calculator.cgpaBefore(plan, fall(2026)).isEmpty());
    }

    @Test
    void sgpaForOneTerm() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 90);
        graded(plan, "CSC165H1", WINTER_2027, 64);
        assertEquals(4.0, calculator.sgpa(plan, fall(2026)).getAsDouble(), DELTA);
        assertEquals(2.0, calculator.sgpa(plan, winter(2027)).getAsDouble(), DELTA);
    }

    @Test
    void fullYearCountsTowardWinterSgpa() {
        Plan plan = twoYears();
        graded(plan, "MAT137Y1", FULL_YEAR_2026, 90);
        assertTrue(calculator.sgpa(plan, fall(2026)).isEmpty());
        assertEquals(4.0, calculator.sgpa(plan, winter(2027)).getAsDouble(), DELTA);
    }

    @Test
    void projectedUsesCurrentAverage() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 90);
        PlannedCourse inProgress = plan.addCourse("CSC165H1", WINTER_2027);
        plan.replaceCourse(inProgress.withGrade(new CourseGrade(null, List.of(
                new Assessment("Midterm", 40, 63.6),
                new Assessment("Final", 60, null)))));
        assertEquals(4.0, calculator.cgpa(plan).getAsDouble(), DELTA);
        assertEquals(3.0, calculator.projectedCgpa(plan).getAsDouble(), DELTA);
    }

    @Test
    void failedThenPassedBothCount() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 40);
        graded(plan, "CSC148H1", FALL_2027, 90);
        assertEquals(2.0, calculator.cgpa(plan).getAsDouble(), DELTA);
    }

    @Test
    void higherRepeatOfPassedCourseReplacesFirst() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 64);
        graded(plan, "CSC148H1", FALL_2027, 90);
        assertEquals(4.0, calculator.cgpa(plan).getAsDouble(), DELTA);
    }

    @Test
    void secondAttemptForCreditStopsAtTwoCredits() {
        Plan plan = twoYears();
        plan.addTerm(winter(2028));
        for (String code : List.of("CSC148H1", "CSC165H1", "MAT135H1", "MAT136H1")) {
            graded(plan, code, FALL_2026, 64);
            graded(plan, code, FALL_2027, 90);
        }
        graded(plan, "PSY100H1", FALL_2026, 64);
        graded(plan, "PSY100H1", WINTER_2028, 90);
        assertEquals((4 * 4.0 * 0.5 + 2.0 * 0.5) / 2.5, calculator.cgpa(plan).getAsDouble(), DELTA);
    }

    @Test
    void secondAttemptThatWouldPassTheCapDoesNotApply() {
        Plan plan = twoYears();
        plan.addTerm(winter(2028));
        for (String code : List.of("CSC148H1", "CSC165H1", "MAT135H1")) {
            graded(plan, code, FALL_2026, 64);
            graded(plan, code, FALL_2027, 90);
        }
        graded(plan, "MAT137Y1", FULL_YEAR_2026, 64);
        graded(plan, "MAT137Y1", FULL_YEAR_2027, 90);
        assertEquals((3 * 4.0 * 0.5 + 2.0 * 1.0) / 2.5, calculator.cgpa(plan).getAsDouble(), DELTA);
    }

    @Test
    void lowerRepeatOfPassedCourseIsExtra() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 90);
        graded(plan, "CSC148H1", FALL_2027, 64);
        assertEquals(4.0, calculator.cgpa(plan).getAsDouble(), DELTA);
    }

    @Test
    void toStringNamesTheClass() {
        assertTrue(calculator.toString().startsWith("GpaCalculator{"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(GpaCalculator.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
