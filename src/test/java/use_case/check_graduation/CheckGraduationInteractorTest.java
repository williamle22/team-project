package use_case.check_graduation;

import data_access.catalog.InMemoryCatalogRepository;
import data_access.degree.ResourceDegreeRepository;
import data_access.plan.InMemoryPlanRepository;
import entity.catalog.CatalogVersion;
import entity.catalog.Completion;
import entity.catalog.CompletionRow;
import entity.catalog.Course;
import entity.catalog.Enrolment;
import entity.catalog.EnrolmentKind;
import entity.catalog.Program;
import entity.catalog.Section;
import entity.degree.DegreeType;
import entity.grades.CourseGrade;
import entity.plan.CreditWeights;
import entity.plan.GpaCalculator;
import entity.plan.Plan;
import entity.plan.ProgramStatus;
import entity.plan.TransferCredit;
import entity.requirement.CourseFilter;
import entity.requirement.ProfileReq;
import entity.requirement.RequirementPrinter;
import entity.term.AcademicTerm;
import entity.term.Placement;
import org.junit.jupiter.api.Test;
import use_case.CatalogReader;
import use_case.DegreeReader;
import use_case.SnapshotFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static entity.catalog.ProgramType.MAJOR;
import static entity.catalog.ProgramType.MINOR;
import static entity.catalog.RowKind.ALLOCATION;
import static entity.catalog.RowKind.CONSTRAINT;
import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.completion;
import static testutil.CatalogFixtures.completionRow;
import static testutil.CatalogFixtures.course;
import static testutil.CatalogFixtures.credits;
import static testutil.CatalogFixtures.creditsIn;
import static testutil.CatalogFixtures.program;
import static testutil.CatalogFixtures.req;

class CheckGraduationInteractorTest {
    private static final Placement FALL_2025 = Placement.fallWinter(2025, Section.F);
    private static final Placement FULL_YEAR_2025 = Placement.fallWinter(2025, Section.Y);
    private static final Placement FULL_YEAR_2026 = Placement.fallWinter(2026, Section.Y);
    private static final String[] A = {"AAA301Y1", "AAA302Y1", "AAA303Y1", "AAA304Y1", "AAA305Y1", "AAA306Y1"};
    private static final String[] B = {"BBB301Y1", "BBB302Y1", "BBB303Y1", "BBB304Y1", "BBB305Y1", "BBB306Y1"};
    private static final RequirementPrinter PRINTER = new RequirementPrinter();
    private static final Enrolment OPEN = new Enrolment(EnrolmentKind.OPEN, credits(0, CourseFilter.any()),
            List.of(), false, List.of("enrolment note"), "Open enrolment.");

    private static final Program MAJOR_A = new Program("ASMAJ0001", MAJOR, "0001", "A Major", OPEN,
            new Completion(List.of(
                    completionRow(1, ALLOCATION, null, null, creditsIn(6, "AAA"), "raw"),
                    completionRow(2, CONSTRAINT, null, 1,
                            credits(1, CourseFilter.builder().levels(List.of(300, 400)).build()), "raw"),
                    new CompletionRow(3, ALLOCATION, null, null, null, List.of(), req("CSC108H1"),
                            new ProfileReq("has_transfer_credits", true, "transfer students only", false), false,
                            List.of(), "raw")),
                    List.of(), List.of(), List.of("completion note"), "raw"),
            List.of());
    private static final Program MAJOR_B = program("ASMAJ0002", MAJOR,
            completion(List.of(completionRow(1, ALLOCATION, null, null, creditsIn(6, "BBB"), "raw"))));
    private static final Program NO_COMPLETION = new Program("ASMIN0003", MINOR, "0003", "C Minor", OPEN, null,
            List.of());
    private static final Program OPTIONS = program("ASMIN0004", MINOR, completion(List.of(
            completionRow(1, ALLOCATION, null, null, null, "raw"),
            completionRow(2, ALLOCATION, null, 1, req("CSC108H1"), "raw"))));
    private static final Program SHARED = new Program("ASMIN0005", MINOR, "0005", "E Minor", OPEN,
            new Completion(List.of(
                    completionRow(1, ALLOCATION, null, null, creditsIn(1, "AAA"), "raw"),
                    completionRow(2, ALLOCATION, null, null, creditsIn(1, "AAA"), "raw")),
                    List.of(), List.of(), List.of("completion note"), "raw"),
            List.of());

    private static final InMemoryCatalogRepository CATALOG = new InMemoryCatalogRepository(
            Stream.of(List.of(A), List.of(B), List.of("CSC108H1", "CSC148H1", "CSC165H1"))
                    .flatMap(List::stream).map(c -> course(c)).collect(Collectors.toList()),
            List.of(MAJOR_A, MAJOR_B, NO_COMPLETION, OPTIONS, SHARED),
            new CatalogVersion("2026-2027", Instant.parse("2026-11-05T18:34:42Z")));
    private static final DegreeReader TEST_DEGREE = new ResourceDegreeRepository("/fixtures/test-degree.json");

    private final InMemoryPlanRepository plans = new InMemoryPlanRepository();
    private final RecordingGraduationPresenter presenter = new RecordingGraduationPresenter();

    private static Plan plan(String... programs) {
        Plan p = Plan.create("test");
        for (int year = 2025; year <= 2026; year++) {
            p.addTerm(AcademicTerm.fall(year));
            p.addTerm(AcademicTerm.winter(year + 1));
            p.addTerm(AcademicTerm.summer(year + 1));
        }
        p.setDegree(DegreeType.HBSC);
        for (String code : programs) {
            p.addProgram(code, ProgramStatus.ENROLLED);
        }
        return p;
    }

    private static void passed(Plan p, String... codes) {
        for (String code : codes) {
            Placement placement = code.charAt(6) == 'Y' ? FULL_YEAR_2025 : FALL_2025;
            p.replaceCourse(p.addCourse(code, placement).withGrade(new CourseGrade(80, List.of())));
        }
    }

    private static void planned(Plan p, String... codes) {
        for (String code : codes) {
            p.addCourse(code, FULL_YEAR_2026);
        }
    }

    private CheckGraduationInteractor interactor(CatalogReader catalog, DegreeReader degrees) {
        return new CheckGraduationInteractor(plans, catalog, degrees,
                new SnapshotFactory(catalog, new GpaCalculator(CreditWeights.FROM_CODE)), presenter);
    }

    private CheckGraduationOutputData run(Plan p, boolean projected) {
        return run(p, projected, TEST_DEGREE);
    }

    private CheckGraduationOutputData run(Plan p, boolean projected, DegreeReader degrees) {
        plans.save(p);
        interactor(CATALOG, degrees).execute(new CheckGraduationInputData(p.getId(), projected));
        assertEquals(List.of("success"), presenter.calls);
        return presenter.data;
    }

    private String fail(CatalogReader catalog, UUID planId) {
        interactor(catalog, TEST_DEGREE).execute(new CheckGraduationInputData(planId, true));
        assertEquals(List.of("fail"), presenter.calls);
        return presenter.failMessage;
    }

    private static ProgramSummary summary(CheckGraduationOutputData data, String code) {
        for (ProgramSummary p : data.getPrograms()) {
            if (p.getCode().equals(code)) {
                return p;
            }
        }
        throw new AssertionError("no program " + code + " in " + data);
    }

    private static List<String> labels(List<RowLine> lines) {
        List<String> labels = new ArrayList<>();
        for (RowLine line : lines) {
            labels.add(line.getLabel());
        }
        return labels;
    }

    private static Plan twoMajors() {
        Plan p = plan("ASMAJ0001", "ASMAJ0002");
        planned(p, A);
        planned(p, B);
        return p;
    }

    @Test
    void catalogNotLoaded() {
        assertEquals("Course data is still loading.", fail(InMemoryCatalogRepository.empty(), UUID.randomUUID()));
    }

    @Test
    void planMissing() {
        assertEquals("That plan no longer exists.", fail(CATALOG, UUID.randomUUID()));
    }

    @Test
    void noDegree() {
        Plan p = Plan.create("test");
        plans.save(p);

        assertEquals("Choose a degree for this plan.", fail(CATALOG, p.getId()));
    }

    @Test
    void projectedCountsPlannedCourses() {
        CheckGraduationOutputData data = run(twoMajors(), true);

        assertTrue(data.isProjected());
        assertEquals(MET, data.getOverall());
        assertEquals(MET, data.getDegree().getStatus());
        assertEquals(MET, data.getCombination().getStatus());
        assertEquals(MET, summary(data, "ASMAJ0001").getCompletion().getStatus());
        assertEquals(MET, summary(data, "ASMAJ0002").getCompletion().getStatus());
    }

    @Test
    void completedOnlyIgnoresPlannedCourses() {
        Plan p = plan("ASMAJ0001", "ASMAJ0002");
        passed(p, A);
        planned(p, B);

        CheckGraduationOutputData data = run(p, false);

        assertFalse(data.isProjected());
        assertEquals(MET, summary(data, "ASMAJ0001").getCompletion().getStatus());
        assertEquals(NOT_MET, summary(data, "ASMAJ0002").getCompletion().getStatus());
        assertEquals(NOT_MET, data.getCombination().getStatus());
        assertEquals(Optional.of("needs 2 Majors completed"), data.getCombination().getReason());
        assertEquals(NOT_MET, data.getOverall());
    }

    @Test
    void degreeAndCombinationLines() {
        CheckGraduationOutputData data = run(twoMajors(), true);

        assertEquals("Degree", data.getDegree().getLabel());
        assertEquals("Honours Bachelor of Science", data.getDegree().getText());
        assertEquals("Programs", data.getCombination().getLabel());
        assertEquals("2 Majors", data.getCombination().getText());
        assertEquals(Optional.empty(), data.getCombination().getReason());
    }

    @Test
    void extraIncompleteProgramDoesntBlock() {
        Plan p = plan("ASMAJ0001", "ASMAJ0002", "ASMIN0004");
        planned(p, A);
        planned(p, B);

        CheckGraduationOutputData data = run(p, true);

        assertEquals(NOT_MET, summary(data, "ASMIN0004").getCompletion().getStatus());
        assertEquals(MET, data.getOverall());
    }

    @Test
    void unknownProgramIsReported() {
        CheckGraduationOutputData data = run(plan("ASMAJ9999"), true);
        RowLine unknown = new RowLine("Enrolment", "Not in the current calendar", REVIEW, null, List.of(),
                List.of());

        assertEquals(new ProgramSummary("ASMAJ9999", "ASMAJ9999", unknown,
                        new RowLine("Completion", "Not in the current calendar", REVIEW, null, List.of(), List.of()),
                        List.of()),
                summary(data, "ASMAJ9999"));
        assertEquals(NOT_MET, data.getCombination().getStatus());
    }

    @Test
    void noCompletionInformation() {
        ProgramSummary p = summary(run(plan("ASMIN0003"), true), "ASMIN0003");

        assertEquals(new RowLine("Completion", "Completion requirements", REVIEW, "no completion information",
                List.of(), List.of()), p.getCompletion());
        assertEquals(new RowLine("Enrolment", "Open enrolment.", MET, "meets the enrolment requirements",
                List.of(), List.of()), p.getEnrolment());
        assertEquals("C Minor", p.getTitle());
        assertEquals(List.of("enrolment note"), p.getNotes());
    }

    @Test
    void noEnrolmentInformation() {
        ProgramSummary p = summary(run(twoMajors(), true), "ASMAJ0002");

        assertEquals(new RowLine("Enrolment", "Enrolment requirements", REVIEW, "no enrolment information",
                List.of(), List.of()), p.getEnrolment());
    }

    @Test
    void rowsKeepCalendarOrderAndNest() {
        CheckGraduationOutputData data = run(twoMajors(), true);
        RowLine degreeRow = data.getDegree().getChildren().get(0);
        RowLine majorRow = summary(data, "ASMAJ0001").getCompletion().getChildren().get(0);

        assertEquals(List.of("Requirement 1"), labels(data.getDegree().getChildren()));
        assertEquals(List.of("Requirement 2", "Requirement 3"), labels(degreeRow.getChildren()));
        assertEquals(PRINTER.print(creditsIn(6, "AAA")), majorRow.getText());
        assertEquals(List.of(A), majorRow.getClaimed());
        assertEquals(List.of("Requirement 2"), labels(majorRow.getChildren()));
    }

    @Test
    void inapplicableRowsAreHidden() {
        RowLine completion = summary(run(twoMajors(), true), "ASMAJ0001").getCompletion();

        assertEquals(List.of("Requirement 1"), labels(completion.getChildren()));
        assertEquals(MET, completion.getStatus());
    }

    @Test
    void optionGroupText() {
        Plan p = plan("ASMIN0004");
        passed(p, "CSC108H1");

        RowLine group = summary(run(p, true), "ASMIN0004").getCompletion().getChildren().get(0);

        assertEquals("One of these options", group.getText());
        assertEquals(MET, group.getStatus());
        assertEquals(PRINTER.print(req("CSC108H1")), group.getChildren().get(0).getText());
    }

    @Test
    void reasonFromRowOrDetail() {
        Plan p = plan();
        passed(p, "CSC108H1", "CSC148H1", "CSC165H1");

        RowLine total = run(p, false).getDegree().getChildren().get(0);

        assertEquals(Optional.of("1.5 of 3.0 credits"), total.getReason());
        assertEquals(Optional.of("1.5 credits fall under this limit; only 1.0 can count"),
                total.getChildren().get(1).getReason());
    }

    @Test
    void metRowsHaveNoReason() {
        RowLine total = run(twoMajors(), true).getDegree().getChildren().get(0);

        assertEquals(MET, total.getStatus());
        assertEquals(Optional.empty(), total.getReason());
    }

    @Test
    void programNotesCollectedInOrder() {
        Plan p = plan("ASMIN0005");
        planned(p, A);

        List<String> notes = summary(run(p, true), "ASMIN0005").getNotes();

        assertEquals(List.of("enrolment note", "completion note"), notes);
    }

    @Test
    void degreeNotesComeFromTheDegreeFile() {
        ResourceDegreeRepository real = new ResourceDegreeRepository();

        CheckGraduationOutputData data = run(twoMajors(), true, real);

        assertEquals(real.findDegree(DegreeType.HBSC).orElseThrow().getCompletion().getNotes(), data.getNotes());
        assertFalse(data.getNotes().isEmpty());
    }

    @Test
    void creditsOutsideTheCalendarAreNoted() {
        Plan p = plan("ASMAJ0001");
        passed(p, "XYZ101H1");
        planned(p, "XYZ101H1", "CSC108H1");
        p.addTransferCredit(new TransferCredit("ABC123H1", 0.5));
        p.addTransferCredit(new TransferCredit("MAT1**", 0.5));
        p.addTransferCredit(new TransferCredit("USP***", 3.5));
        p.addTransferCredit(new TransferCredit("CSC148H1", 0.5));

        assertEquals(List.of(
                "MAT1** is an unspecified transfer credit; it counts toward a requirement only with the "
                        + "department's permission",
                "XYZ101H1 isn't in the current calendar, so it doesn't count toward any requirement",
                "ABC123H1 isn't in the current calendar, so it doesn't count toward any requirement"),
                run(p, true).getNotes());
    }

    @Test
    void programsKeepPlanOrder() {
        CheckGraduationOutputData data = run(plan("ASMAJ0002", "ASMAJ9999", "ASMAJ0001"), true);
        List<String> codes = new ArrayList<>();
        for (ProgramSummary p : data.getPrograms()) {
            codes.add(p.getCode());
        }

        assertEquals(List.of("ASMAJ0002", "ASMAJ9999", "ASMAJ0001"), codes);
    }
}
