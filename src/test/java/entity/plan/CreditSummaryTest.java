package entity.plan;

import entity.catalog.Section;
import entity.grades.CourseGrade;
import entity.term.Placement;
import org.junit.jupiter.api.Test;

import static entity.term.AcademicTerm.fall;
import static entity.term.AcademicTerm.summer;
import static entity.term.AcademicTerm.winter;
import static org.junit.jupiter.api.Assertions.*;

class CreditSummaryTest {
    private static final double DELTA = 1e-9;
    private static final Placement FALL_2026 = Placement.fallWinter(2026, Section.F);
    private static final Placement WINTER_2027 = Placement.fallWinter(2026, Section.S);
    private static final Placement FULL_YEAR_2026 = Placement.fallWinter(2026, Section.Y);
    private static final Placement SUMMER_2027 = Placement.summer(2027, Section.F);
    private static final Placement FALL_2027 = Placement.fallWinter(2027, Section.F);

    private static Plan twoYears() {
        Plan plan = Plan.create("My plan");
        plan.addTerm(fall(2026));
        plan.addTerm(winter(2027));
        plan.addTerm(summer(2027));
        plan.addTerm(fall(2027));
        return plan;
    }

    private static PlannedCourse graded(Plan plan, String code, Placement placement, int finalGrade) {
        PlannedCourse c = plan.addCourse(code, placement);
        PlannedCourse withGrade = c.withGrade(CourseGrade.empty().withFinalGrade(finalGrade));
        plan.replaceCourse(withGrade);
        return withGrade;
    }

    private static CreditSummary summary(Plan plan) {
        return CreditSummary.of(plan, CreditWeights.FROM_CODE, winter(2027));
    }

    @Test
    void oneCourseInEachBucket() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 70);
        graded(plan, "CSC165H1", FALL_2026, 40);
        plan.addCourse("MAT135H1", FALL_2026);
        plan.addCourse("CSC207H1", WINTER_2027);
        plan.addCourse("CSC209H1", SUMMER_2027);
        plan.addCourse("CSC236H1", FALL_2027);
        CreditSummary s = summary(plan);
        assertEquals(0.5, s.getCompleted(), DELTA);
        assertEquals(0.5, s.getFailed(), DELTA);
        assertEquals(0.5, s.getAwaitingGrade(), DELTA);
        assertEquals(0.5, s.getInProgress(), DELTA);
        assertEquals(1.0, s.getPlanned(), DELTA);
        assertEquals(0, s.getTransfer(), DELTA);
        assertEquals(2.5, s.total(), DELTA);
    }

    @Test
    void fullYearSpanningCurrentIsInProgress() {
        Plan plan = twoYears();
        plan.addCourse("MAT137Y1", FULL_YEAR_2026);
        assertEquals(1.0, summary(plan).getInProgress(), DELTA);
    }

    @Test
    void transferCountsAsCompleted() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 70);
        plan.addTransferCredit(new TransferCredit("MAT135H1", 0.5));
        plan.addTransferCredit(new TransferCredit("PHL100Y1", 1.0));
        CreditSummary s = summary(plan);
        assertEquals(1.5, s.getTransfer(), DELTA);
        assertEquals(2.0, s.getCompleted(), DELTA);
    }

    @Test
    void crNcrPassIsCompleted() {
        Plan plan = twoYears();
        PlannedCourse c = graded(plan, "PSY100H1", FALL_2026, 55);
        plan.replaceCourse(c.withCrNcr(true));
        assertEquals(0.5, summary(plan).getCompleted(), DELTA);
    }

    @Test
    void repeatedPassCountsOnce() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 64);
        graded(plan, "CSC148H1", FALL_2027, 90);
        assertEquals(0.5, summary(plan).getCompleted(), DELTA);
    }

    @Test
    void failedThenPassedIsOneOfEach() {
        Plan plan = twoYears();
        graded(plan, "CSC148H1", FALL_2026, 40);
        graded(plan, "CSC148H1", FALL_2027, 70);
        CreditSummary s = summary(plan);
        assertEquals(0.5, s.getCompleted(), DELTA);
        assertEquals(0.5, s.getFailed(), DELTA);
    }
}
