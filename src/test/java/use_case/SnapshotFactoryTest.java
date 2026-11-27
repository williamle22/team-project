package use_case;

import data_access.catalog.InMemoryCatalogRepository;
import entity.catalog.CatalogVersion;
import entity.catalog.Section;
import entity.evaluation.AcademicSnapshot;
import entity.evaluation.CourseRecord;
import entity.grades.CourseGrade;
import entity.plan.CreditWeights;
import entity.plan.GpaCalculator;
import entity.plan.Plan;
import entity.plan.PlannedCourse;
import entity.plan.ProgramStatus;
import entity.plan.TransferCredit;
import entity.term.AcademicTerm;
import entity.term.Placement;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.course;

class SnapshotFactoryTest {
    private static final Placement FALL_2025 = Placement.fallWinter(2025, Section.F);
    private static final Placement WINTER_2026 = Placement.fallWinter(2025, Section.S);
    private static final Placement FULL_YEAR_2025 = Placement.fallWinter(2025, Section.Y);
    private static final Placement SUMMER_2026 = Placement.summer(2026, Section.F);
    private static final Placement FALL_2026 = Placement.fallWinter(2026, Section.F);
    private static final Placement WINTER_2027 = Placement.fallWinter(2026, Section.S);

    private static final InMemoryCatalogRepository CATALOG = new InMemoryCatalogRepository(
            Stream.of("CSC108H1", "CSC148H1", "CSC165H1", "CSC207H1", "MAT137Y1", "STA130H1")
                    .map(c -> course(c)).collect(Collectors.toList()),
            List.of(), new CatalogVersion("2026-2027", Instant.parse("2026-11-05T18:34:42Z")));

    private final SnapshotFactory factory = new SnapshotFactory(CATALOG, new GpaCalculator(CreditWeights.FROM_CODE));

    private static Plan plan() {
        Plan p = Plan.create("test");
        for (int year = 2025; year <= 2026; year++) {
            p.addTerm(AcademicTerm.fall(year));
            p.addTerm(AcademicTerm.winter(year + 1));
            p.addTerm(AcademicTerm.summer(year + 1));
        }
        return p;
    }

    private static PlannedCourse add(Plan p, String code, Placement placement) {
        return p.addCourse(code, placement);
    }

    private static PlannedCourse add(Plan p, String code, Placement placement, int grade) {
        PlannedCourse c = p.addCourse(code, placement).withGrade(new CourseGrade(grade, List.of()));
        p.replaceCourse(c);
        return c;
    }

    private static Optional<Object> yearOfStudy(AcademicSnapshot s) {
        return s.profileFact("min_year_of_study");
    }

    @Test
    void earlierCourseIsTaken() {
        Plan p = plan();
        add(p, "CSC108H1", FALL_2025, 80);
        PlannedCourse target = add(p, "CSC148H1", WINTER_2026);

        AcademicSnapshot s = factory.forCourse(p, target);

        assertEquals(Set.of("CSC108H1"), s.getTaken().keySet());
        assertEquals(Optional.of(80), s.getTaken().get("CSC108H1").getGrade());
        assertTrue(s.getConcurrent().isEmpty());
        assertEquals(Optional.of(AcademicTerm.winter(2026)), s.getAsOf());
        assertEquals(OptionalDouble.of(3.7), s.getCgpa());
    }

    @Test
    void sameTermIsConcurrent() {
        Plan p = plan();
        add(p, "CSC165H1", WINTER_2026);
        add(p, "CSC207H1", FALL_2026);
        PlannedCourse target = add(p, "CSC148H1", WINTER_2026);

        AcademicSnapshot s = factory.forCourse(p, target);

        assertTrue(s.getTaken().isEmpty());
        assertEquals(Set.of("CSC165H1"), s.getConcurrent().keySet());
    }

    @Test
    void fullYearOverlapsWinter() {
        Plan p = plan();
        add(p, "MAT137Y1", FULL_YEAR_2025);
        PlannedCourse winter = add(p, "CSC148H1", WINTER_2026);
        PlannedCourse fall = add(p, "CSC108H1", FALL_2025);

        assertEquals(Set.of("MAT137Y1"), factory.forCourse(p, winter).getConcurrent().keySet());
        assertEquals(Set.of("MAT137Y1"), factory.forCourse(p, fall).getConcurrent().keySet());
    }

    @Test
    void fullYearTakenBySummer() {
        Plan p = plan();
        add(p, "MAT137Y1", FULL_YEAR_2025, 70);
        PlannedCourse target = add(p, "STA130H1", SUMMER_2026);

        AcademicSnapshot s = factory.forCourse(p, target);

        assertEquals(Set.of("MAT137Y1"), s.getTaken().keySet());
        assertTrue(s.getConcurrent().isEmpty());
    }

    @Test
    void failedCourseExcluded() {
        Plan p = plan();
        add(p, "CSC108H1", FALL_2025, 40);
        add(p, "CSC165H1", WINTER_2026, 45);
        PlannedCourse target = add(p, "CSC148H1", WINTER_2026);

        AcademicSnapshot s = factory.forCourse(p, target);

        assertTrue(s.getTaken().isEmpty());
        assertTrue(s.getConcurrent().isEmpty());
    }

    @Test
    void retakeKeepsBestGrade() {
        Plan p = plan();
        add(p, "CSC108H1", FALL_2025, 55);
        add(p, "CSC108H1", SUMMER_2026, 75);
        add(p, "CSC108H1", WINTER_2026);
        add(p, "CSC108H1", WINTER_2027);
        PlannedCourse target = add(p, "CSC148H1", WINTER_2027);

        AcademicSnapshot s = factory.forCourse(p, target);

        assertEquals(Optional.of(75), s.getTaken().get("CSC108H1").getGrade());
        assertTrue(s.getConcurrent().isEmpty());
    }

    @Test
    void unknownGradeDoesntReplaceKnown() {
        Plan p = plan();
        add(p, "CSC108H1", FALL_2025, 60);
        add(p, "CSC108H1", WINTER_2026);
        PlannedCourse target = add(p, "CSC148H1", FALL_2026);

        CourseRecord r = factory.forCourse(p, target).getTaken().get("CSC108H1");

        assertEquals(Optional.of(60), r.getGrade());
    }

    @Test
    void unknownCodeIsSkipped() {
        Plan p = plan();
        add(p, "XYZ123H1", FALL_2025);
        add(p, "XYZ123H1", SUMMER_2026);
        add(p, "ABC999H1", WINTER_2027);
        p.addTransferCredit(new TransferCredit("ABC123H1", 0.5));
        PlannedCourse target = add(p, "CSC207H1", FALL_2026);

        assertTrue(factory.forCourse(p, target).getTaken().isEmpty());
    }

    @Test
    void unspecifiedTransferCreditSkipped() {
        Plan p = plan();
        p.addTransferCredit(new TransferCredit("MAT1**", 0.5));
        p.addTransferCredit(new TransferCredit("USP***", 3.5));
        PlannedCourse target = add(p, "CSC148H1", FALL_2025);

        AcademicSnapshot s = factory.forCourse(p, target);

        assertTrue(s.getTaken().isEmpty());
        assertEquals(Optional.of(2), yearOfStudy(s));
        assertEquals(Optional.of(true), s.profileFact("has_transfer_credits"));
    }

    @Test
    void transferCreditIsTaken() {
        Plan p = plan();
        p.addTransferCredit(new TransferCredit("CSC108H1", 0.5));
        PlannedCourse target = add(p, "CSC148H1", FALL_2025);

        AcademicSnapshot s = factory.forCourse(p, target);
        CourseRecord r = s.getTaken().get("CSC108H1");

        assertTrue(r.isTransfer());
        assertEquals(Optional.empty(), r.getGrade());
        assertEquals(Optional.of(true), s.profileFact("has_transfer_credits"));
    }

    @Test
    void noTransferCredits() {
        Plan p = plan();
        PlannedCourse target = add(p, "CSC148H1", FALL_2025);

        AcademicSnapshot s = factory.forCourse(p, target);

        assertEquals(Optional.of(false), s.profileFact("has_transfer_credits"));
    }

    @Test
    void gradedCourseBeatsTransferCredit() {
        Plan p = plan();
        p.addTransferCredit(new TransferCredit("CSC108H1", 0.5));
        add(p, "CSC108H1", FALL_2025, 70);
        PlannedCourse target = add(p, "CSC148H1", WINTER_2026);

        CourseRecord r = factory.forCourse(p, target).getTaken().get("CSC108H1");

        assertFalse(r.isTransfer());
        assertEquals(Optional.of(70), r.getGrade());
    }

    @Test
    void targetNotInEitherMap() {
        Plan p = plan();
        add(p, "CSC148H1", FALL_2025, 60);
        p.addTransferCredit(new TransferCredit("CSC148H1", 0.5));
        add(p, "CSC165H1", WINTER_2026);
        PlannedCourse target = add(p, "CSC148H1", WINTER_2026);

        AcademicSnapshot s = factory.forCourse(p, target);

        assertTrue(s.getTaken().isEmpty());
        assertEquals(Set.of("CSC165H1"), s.getConcurrent().keySet());
    }

    @Test
    void programsAndEmptyCgpa() {
        Plan p = plan();
        p.addProgram("ASMAJ1689", ProgramStatus.ENROLLED);
        p.addProgram("ASSPE1689", ProgramStatus.INTENDED);
        PlannedCourse target = add(p, "CSC148H1", FALL_2025);

        AcademicSnapshot s = factory.forCourse(p, target);

        assertEquals(Set.of("ASMAJ1689"), s.getEnrolledPrograms());
        assertEquals(Set.of("ASSPE1689"), s.getIntendedPrograms());
        assertEquals(OptionalDouble.empty(), s.getCgpa());
    }

    @Test
    void yearOfStudyFromCredits() {
        Plan p = plan();
        add(p, "CSC108H1", FALL_2025, 60);
        add(p, "CSC108H1", WINTER_2026, 70);
        add(p, "MAT137Y1", FULL_YEAR_2025, 80);
        add(p, "CSC165H1", FALL_2025);
        add(p, "STA130H1", FALL_2025, 30);
        add(p, "CSC207H1", FALL_2026, 90);
        p.addTransferCredit(new TransferCredit("ABC1**", 2.0));
        PlannedCourse target = add(p, "CSC148H1", FALL_2026);

        assertEquals(Optional.of(1), yearOfStudy(factory.forCourse(p, target)));
        assertEquals(Optional.of(2), yearOfStudy(factory.wholePlan(p)));
    }

    @Test
    void yearOfStudyThresholds() {
        double[][] cases = {{3.5, 1}, {4.0, 2}, {8.5, 2}, {9.0, 3}, {13.5, 3}, {14.0, 4}};

        for (double[] c : cases) {
            Plan p = plan();
            p.addTransferCredit(new TransferCredit("ABC1**", c[0]));

            assertEquals(Optional.of((int) c[1]), yearOfStudy(factory.wholePlan(p)), "credits " + c[0]);
        }
    }

    @Test
    void wholePlanTakesEverything() {
        Plan p = plan();
        add(p, "CSC108H1", FALL_2025, 80);
        add(p, "CSC148H1", WINTER_2026);
        add(p, "CSC165H1", WINTER_2026, 40);
        add(p, "CSC207H1", WINTER_2027, 70);
        p.addTransferCredit(new TransferCredit("STA130H1", 0.5));

        AcademicSnapshot s = factory.wholePlan(p);

        assertEquals(List.of("CSC108H1", "CSC148H1", "CSC207H1", "STA130H1"), List.copyOf(s.getTaken().keySet()));
        assertTrue(s.getConcurrent().isEmpty());
        assertEquals(Optional.empty(), s.getAsOf());
        assertEquals(new GpaCalculator(CreditWeights.FROM_CODE).cgpa(p), s.getCgpa());
    }

    @Test
    void rejectsNulls() {
        GpaCalculator gpa = new GpaCalculator(CreditWeights.FROM_CODE);

        assertThrows(NullPointerException.class, () -> new SnapshotFactory(null, gpa));
        assertThrows(NullPointerException.class, () -> new SnapshotFactory(CATALOG, null));
    }
}
