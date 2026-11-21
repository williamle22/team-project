package use_case.check_eligibility;

import data_access.catalog.InMemoryCatalogRepository;
import data_access.plan.InMemoryPlanRepository;
import entity.catalog.CatalogVersion;
import entity.catalog.Course;
import entity.catalog.ExclusionItem;
import entity.catalog.Exclusions;
import entity.catalog.Requisite;
import entity.catalog.Section;
import entity.catalog.Sessions;
import entity.evaluation.EligibilityEvaluator;
import entity.evaluation.Status;
import entity.grades.CourseGrade;
import entity.plan.CreditWeights;
import entity.plan.GpaCalculator;
import entity.plan.Plan;
import entity.plan.PlannedCourse;
import entity.plan.TransferCredit;
import entity.requirement.OrReq;
import entity.requirement.Requirement;
import entity.requirement.TextKind;
import entity.term.AcademicTerm;
import entity.term.Placement;
import org.junit.jupiter.api.Test;
import use_case.CatalogReader;
import use_case.CurrentAcademicYear;
import use_case.SnapshotFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.and;
import static testutil.CatalogFixtures.choose;
import static testutil.CatalogFixtures.course;
import static testutil.CatalogFixtures.excludesCourses;
import static testutil.CatalogFixtures.not;
import static testutil.CatalogFixtures.or;
import static testutil.CatalogFixtures.req;
import static testutil.CatalogFixtures.requisite;
import static testutil.CatalogFixtures.text;

class CheckEligibilityInteractorTest {
    private static final Placement FALL_2025 = Placement.fallWinter(2025, Section.F);
    private static final Placement FALL_2026 = Placement.fallWinter(2026, Section.F);
    private static final Placement WINTER_2027 = Placement.fallWinter(2026, Section.S);
    private static final Placement FULL_YEAR_2026 = Placement.fallWinter(2026, Section.Y);
    private static final Placement FULL_YEAR_2027 = Placement.fallWinter(2027, Section.Y);
    private static final Sessions FALL_AND_WINTER = new Sessions(List.of(Section.F, Section.S), List.of());
    private static final CatalogVersion VERSION =
            new CatalogVersion("2026-2027", Instant.parse("2026-11-05T18:34:42Z"));

    private static final InMemoryCatalogRepository CATALOG = new InMemoryCatalogRepository(List.of(
            offered("CSC108H1", null, null),
            offered("CSC110Y1", null, new Exclusions(List.of(placement("permission of the department"),
                    excludesCourses("CSC108H1")), List.of(), "")),
            offered("CSC111H1", null, exclusions(excludesCourses("CSC148H1"))),
            offered("CSC148H1", req("CSC108H1"), exclusions(excludesCourses("CSC111H1"))),
            course("CSC165H1", null, requisite(req("CSC148H1")), null, List.of(), FALL_AND_WINTER),
            offered("CSC207H1", req("CSC148H1", 60), null),
            offered("CSC263H1", and(or(req("MAT137Y1"), req("STA130H1")), choose(1, req("CSC165H1"),
                    req("CSC111H1")), not(req("CSC111H1"))), null),
            offered("CSC265H1", new OrReq(List.of(req("MAT137Y1"), req("STA130H1")), true), null),
            offered("CSC490H1", text("permission of the instructor"), null),
            offered("CSC495H1", text(""), null),
            offered("ENG253H1", new OrReq(List.of(req("CSC108H1")), true), null),
            offered("FSL322H1", null, exclusions(placement("placement at the FSL400+ level"))),
            offered("STA130H1", and(or(req("CSC108H1"))), null),
            course("MAT137Y1")), List.of(), VERSION);

    private final InMemoryPlanRepository plans = new InMemoryPlanRepository();
    private final RecordingEligibilityPresenter presenter = new RecordingEligibilityPresenter();

    private static Course offered(String code, Requirement prereq, Exclusions exclusions) {
        Requisite prereqs = prereq == null ? null : requisite(prereq);
        return course(code, prereqs, null, exclusions, List.of(), FALL_AND_WINTER);
    }

    private static ExclusionItem placement(String text) {
        return new ExclusionItem(List.of(), List.of(), List.of(), List.of(), false, null, TextKind.PLACEMENT, text);
    }

    private static Exclusions exclusions(ExclusionItem item) {
        return new Exclusions(List.of(item), List.of(), "");
    }

    private static Plan plan() {
        Plan p = Plan.create("test");
        for (int year = 2025; year <= 2027; year++) {
            p.addTerm(AcademicTerm.fall(year));
            p.addTerm(AcademicTerm.winter(year + 1));
            p.addTerm(AcademicTerm.summer(year + 1));
        }
        return p;
    }

    private static PlannedCourse add(Plan p, String code, Placement placement, int grade) {
        PlannedCourse c = p.addCourse(code, placement).withGrade(new CourseGrade(grade, List.of()));
        p.replaceCourse(c);
        return c;
    }

    private CheckEligibilityInteractor interactor(CatalogReader catalog) {
        Clock clock = Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneId.of("America/Toronto"));
        return new CheckEligibilityInteractor(plans, catalog,
                new SnapshotFactory(catalog, new GpaCalculator(CreditWeights.FROM_CODE)),
                new CurrentAcademicYear(clock), new EligibilityEvaluator(), presenter);
    }

    private CheckEligibilityOutputData run(Plan p) {
        plans.save(p);
        interactor(CATALOG).execute(new CheckEligibilityInputData(p.getId()));
        assertEquals(List.of("success"), presenter.calls);
        return presenter.data;
    }

    private CourseEligibility check(Plan p, PlannedCourse c) {
        return run(p).getCourses().get(c.getId());
    }

    private static EligibilityLine section(CourseEligibility c, String title) {
        for (EligibilityLine line : c.getSections()) {
            if (line.getText().equals(title)) {
                return line;
            }
        }
        throw new AssertionError("no section " + title + " in " + c);
    }

    private static List<String> titles(CourseEligibility c) {
        List<String> titles = new ArrayList<>();
        for (EligibilityLine line : c.getSections()) {
            titles.add(line.getText());
        }
        return titles;
    }

    private static EligibilityLine leaf(String text, Status status) {
        return new EligibilityLine(text, status, null, List.of());
    }

    @Test
    void catalogNotLoadedFails() {
        Plan p = plan();
        plans.save(p);

        interactor(InMemoryCatalogRepository.empty()).execute(new CheckEligibilityInputData(p.getId()));

        assertEquals(List.of("fail"), presenter.calls);
        assertEquals("Course data is still loading.", presenter.failMessage);
    }

    @Test
    void missingPlanFails() {
        interactor(CATALOG).execute(new CheckEligibilityInputData(UUID.randomUUID()));

        assertEquals(List.of("fail"), presenter.calls);
        assertEquals("That plan no longer exists.", presenter.failMessage);
    }

    @Test
    void prereqMetEarlier() {
        Plan p = plan();
        add(p, "CSC108H1", FALL_2025, 70);
        PlannedCourse csc148 = p.addCourse("CSC148H1", WINTER_2027);

        CourseEligibility c = check(p, csc148);

        assertEquals(csc148.getId(), c.getPlannedCourseId());
        assertEquals("CSC148H1", c.getCode());
        assertEquals("Winter 2027", c.getPlacementLabel());
        assertEquals(MET, c.getOverall());
        assertEquals(List.of(
                new EligibilityLine("Prerequisites", MET, null, List.of(leaf("CSC108H1", MET))),
                new EligibilityLine("Corequisites", MET, null, List.of(leaf("No corequisites", MET))),
                new EligibilityLine("Exclusions", MET, null, List.of()),
                leaf("Offered in Winter 2027", MET)), c.getSections());
        assertTrue(c.getWarnings().isEmpty());
    }

    @Test
    void prereqSameTermNotMet() {
        Plan p = plan();
        p.addCourse("CSC108H1", WINTER_2027);
        PlannedCourse csc148 = p.addCourse("CSC148H1", WINTER_2027);

        CourseEligibility c = check(p, csc148);

        assertEquals(NOT_MET, c.getOverall());
        EligibilityLine prereqs = section(c, "Prerequisites");
        assertEquals(NOT_MET, prereqs.getStatus());
        assertEquals("CSC108H1", prereqs.getChildren().get(0).getText());
        assertTrue(prereqs.getChildren().get(0).getReason().isPresent());
    }

    @Test
    void coreqSameTermMet() {
        Plan p = plan();
        p.addCourse("CSC148H1", WINTER_2027);
        PlannedCourse csc165 = p.addCourse("CSC165H1", WINTER_2027);

        CourseEligibility c = check(p, csc165);

        assertEquals(MET, section(c, "Corequisites").getStatus());
        assertEquals(MET, c.getOverall());
    }

    @Test
    void excludedEarlierNotMet() {
        Plan p = plan();
        add(p, "CSC148H1", FALL_2025, 80);
        PlannedCourse csc111 = p.addCourse("CSC111H1", WINTER_2027);

        CourseEligibility c = check(p, csc111);

        assertEquals(new EligibilityLine("Exclusions", NOT_MET, null, List.of(leaf("Excluded by CSC148H1", NOT_MET))),
                section(c, "Exclusions"));
        assertEquals(NOT_MET, c.getOverall());
    }

    @Test
    void exclusionTextIsReview() {
        Plan p = plan();
        PlannedCourse fsl = p.addCourse("FSL322H1", FALL_2026);

        CourseEligibility c = check(p, fsl);

        assertEquals(new EligibilityLine("Exclusions", REVIEW, null,
                List.of(leaf("Placement at the FSL400+ level", REVIEW))), section(c, "Exclusions"));
        assertEquals(REVIEW, c.getOverall());
    }

    @Test
    void exclusionReasonsKeepTheirOwnStatus() {
        Plan p = plan();
        add(p, "CSC108H1", FALL_2025, 80);
        PlannedCourse csc110 = p.addCourse("CSC110Y1", FULL_YEAR_2027);

        assertEquals(new EligibilityLine("Exclusions", NOT_MET, null, List.of(
                leaf("Permission of the department", REVIEW), leaf("Excluded by CSC108H1", NOT_MET))),
                section(check(p, csc110), "Exclusions"));
    }

    @Test
    void minGradeNoFinalIsReview() {
        Plan p = plan();
        p.addCourse("CSC148H1", FALL_2025);
        PlannedCourse csc207 = p.addCourse("CSC207H1", FALL_2026);

        CourseEligibility c = check(p, csc207);

        EligibilityLine line = section(c, "Prerequisites").getChildren().get(0);
        assertEquals("CSC148H1 (min. 60%)", line.getText());
        assertEquals(REVIEW, line.getStatus());
        assertEquals(REVIEW, c.getOverall());
    }

    @Test
    void permissionTextIsReview() {
        Plan p = plan();
        PlannedCourse csc490 = p.addCourse("CSC490H1", FALL_2026);

        CourseEligibility c = check(p, csc490);

        EligibilityLine line = section(c, "Prerequisites").getChildren().get(0);
        assertEquals("Permission of the instructor", line.getText());
        assertEquals(REVIEW, line.getStatus());
        assertEquals(REVIEW, c.getOverall());
    }

    @Test
    void notOfferedThisYear() {
        Plan p = plan();
        PlannedCourse mat137 = p.addCourse("MAT137Y1", FULL_YEAR_2026);

        CourseEligibility c = check(p, mat137);

        assertEquals(leaf("Not offered in Fall-Winter 2026-2027 (Y)", NOT_MET), c.getSections().get(3));
        assertEquals(List.of("Not offered in Fall-Winter 2026-2027 (Y)"), c.getWarnings());
        assertEquals(NOT_MET, c.getOverall());
    }

    @Test
    void futureYearSkipsOffering() {
        Plan p = plan();
        PlannedCourse mat137 = p.addCourse("MAT137Y1", FULL_YEAR_2027);

        CourseEligibility c = check(p, mat137);

        assertEquals(List.of("Prerequisites", "Corequisites", "Exclusions"), titles(c));
        assertEquals(MET, c.getOverall());
    }

    @Test
    void unknownCodeIsReview() {
        Plan p = plan();
        PlannedCourse unknown = p.addCourse("XYZ123H1", FALL_2025);
        PlannedCourse later = p.addCourse("CSC108H1", FALL_2026);

        CheckEligibilityOutputData data = run(p);
        CourseEligibility c = data.getCourses().get(unknown.getId());

        assertEquals(new CourseEligibility(unknown.getId(), "XYZ123H1", "Fall 2025", REVIEW,
                List.of(leaf("Not in the current calendar", REVIEW)), List.of()), c);
        assertTrue(data.getCourses().get(later.getId()).getWarnings().isEmpty());
    }

    @Test
    void unknownCodeWarnsOnce() {
        Plan p = plan();
        p.addCourse("XYZ123H1", FALL_2025);
        p.addCourse("XYZ123H1", FALL_2026);
        p.addTransferCredit(new TransferCredit("ABC123H1", 0.5));

        assertEquals(List.of("XYZ123H1 isn't in the current calendar", "ABC123H1 isn't in the current calendar",
                "XYZ123H1 appears 2 times (retake?)"), run(p).getPlanWarnings());
    }

    @Test
    void retakeWarning() {
        Plan p = plan();
        add(p, "CSC108H1", FALL_2025, 40);
        p.addCourse("CSC108H1", FALL_2026);
        p.addCourse("CSC148H1", WINTER_2027);

        assertEquals(List.of("CSC108H1 appears 2 times (retake?)"), run(p).getPlanWarnings());
    }

    @Test
    void exclusionConflictWarning() {
        Plan p = plan();
        p.addCourse("CSC148H1", WINTER_2027);
        p.addCourse("CSC108H1", FALL_2026);
        p.addCourse("CSC111H1", WINTER_2027);

        assertEquals(List.of("CSC148H1 and CSC111H1 exclude each other"), run(p).getPlanWarnings());
    }

    @Test
    void sequentialExclusionIsOnlyACourseResult() {
        Plan p = plan();
        p.addCourse("CSC148H1", FALL_2026);
        PlannedCourse csc111 = p.addCourse("CSC111H1", WINTER_2027);

        CheckEligibilityOutputData data = run(p);

        assertTrue(data.getPlanWarnings().isEmpty());
        assertEquals(NOT_MET, section(data.getCourses().get(csc111.getId()), "Exclusions").getStatus());
    }

    @Test
    void unspecifiedTransferCreditWarning() {
        Plan p = plan();
        p.addTransferCredit(new TransferCredit("MAT1**", 0.5));

        assertEquals(List.of("MAT1** is an unspecified transfer credit; it counts toward a requirement only with "
                + "the department's permission"), run(p).getPlanWarnings());
    }

    @Test
    void genericTransferCreditNoWarning() {
        Plan p = plan();
        p.addTransferCredit(new TransferCredit("USP***", 2.0));
        p.addTransferCredit(new TransferCredit("CSC108H1", 0.5));

        assertTrue(run(p).getPlanWarnings().isEmpty());
    }

    @Test
    void singleChildAndCollapses() {
        Plan p = plan();
        add(p, "CSC108H1", FALL_2025, 70);
        PlannedCourse sta130 = p.addCourse("STA130H1", FALL_2026);

        CourseEligibility c = check(p, sta130);

        assertEquals(new EligibilityLine("Prerequisites", MET, null, List.of(leaf("CSC108H1", MET))),
                section(c, "Prerequisites"));
    }

    @Test
    void treeShowsCombinators() {
        Plan p = plan();
        add(p, "STA130H1", FALL_2025, 70);
        add(p, "CSC165H1", FALL_2025, 70);
        PlannedCourse csc263 = p.addCourse("CSC263H1", FALL_2026);

        EligibilityLine all = section(check(p, csc263), "Prerequisites").getChildren().get(0);

        assertEquals("All of:", all.getText());
        assertEquals(MET, all.getStatus());
        assertNull(all.getReason().orElse(null));

        EligibilityLine one = all.getChildren().get(0);
        assertEquals("One of:", one.getText());
        assertEquals(List.of("MAT137Y1", "STA130H1"), List.of(one.getChildren().get(0).getText(),
                one.getChildren().get(1).getText()));
        assertEquals(MET, one.getChildren().get(1).getStatus());

        EligibilityLine choose = all.getChildren().get(1);
        assertEquals("1 of:", choose.getText());
        assertEquals("1 of 1 met", choose.getReason().get());
        assertEquals(2, choose.getChildren().size());

        EligibilityLine none = all.getChildren().get(2);
        assertEquals("None of:", none.getText());
        assertEquals(MET, none.getStatus());
        assertEquals(List.of("CSC111H1"), List.of(none.getChildren().get(0).getText()));
        assertEquals(NOT_MET, none.getChildren().get(0).getStatus());
    }

    @Test
    void concurrentCombinatorSaysSo() {
        Plan p = plan();
        p.addCourse("STA130H1", FALL_2026);
        PlannedCourse csc265 = p.addCourse("CSC265H1", FALL_2026);

        EligibilityLine one = section(check(p, csc265), "Prerequisites").getChildren().get(0);

        assertEquals("One of (may be taken concurrently):", one.getText());
        assertEquals(MET, one.getStatus());
        assertEquals(2, one.getChildren().size());
    }

    @Test
    void singleChildConcurrentOrDoesntCollapse() {
        Plan p = plan();
        PlannedCourse eng253 = p.addCourse("ENG253H1", FALL_2026);

        EligibilityLine one = section(check(p, eng253), "Prerequisites").getChildren().get(0);

        assertEquals("One of (may be taken concurrently):", one.getText());
        assertEquals("CSC108H1", one.getChildren().get(0).getText());
    }

    @Test
    void emptyTextStaysEmpty() {
        Plan p = plan();
        PlannedCourse csc495 = p.addCourse("CSC495H1", FALL_2026);

        assertEquals("", section(check(p, csc495), "Prerequisites").getChildren().get(0).getText());
    }

    @Test
    void coursesInPlanOrder() {
        Plan p = plan();
        PlannedCourse first = p.addCourse("CSC148H1", WINTER_2027);
        PlannedCourse second = p.addCourse("CSC108H1", FALL_2025);

        CheckEligibilityOutputData data = run(p);

        assertEquals(p.getId(), data.getPlanId());
        assertEquals(List.of(first.getId(), second.getId()), new ArrayList<>(data.getCourses().keySet()));
    }
}
