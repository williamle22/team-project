package entity.evaluation;

import entity.catalog.BreadthCategory;
import entity.requirement.AverageReq;
import entity.requirement.CourseFilter;
import entity.requirement.CreditsReq;
import entity.requirement.GpaReq;
import entity.requirement.OrReq;
import entity.requirement.ProfileReq;
import entity.requirement.Requirement;
import entity.requirement.RowRefReq;
import entity.term.AcademicTerm;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.and;
import static testutil.CatalogFixtures.choose;
import static testutil.CatalogFixtures.concurrentReq;
import static testutil.CatalogFixtures.credits;
import static testutil.CatalogFixtures.creditsIn;
import static testutil.CatalogFixtures.not;
import static testutil.CatalogFixtures.or;
import static testutil.CatalogFixtures.programReq;
import static testutil.CatalogFixtures.record;
import static testutil.CatalogFixtures.req;
import static testutil.CatalogFixtures.text;
import static testutil.CatalogFixtures.withBreadth;

class RequisiteEvaluatorTest {

    private static final CourseFilter CSC = CourseFilter.builder().designators(List.of("CSC")).build();
    private static final CourseFilter GROUP_1 = CourseFilter.builder().lists(List.of("Group 1")).build();
    private static final CourseRecord ANT100 = record(withBreadth("ANT100Y1",
            BreadthCategory.SOCIETY_INSTITUTIONS, BreadthCategory.LIVING_THINGS), 75);
    private static final CourseRecord MAT1 = CourseRecord.unspecifiedTransfer("MAT1**", 2.0).orElseThrow();

    private static EvaluationNode prereq(AcademicSnapshot s, Requirement r) {
        return new RequisiteEvaluator(s, false, MembershipResolver.NONE).evaluate(r);
    }

    private static EvaluationNode coreq(AcademicSnapshot s, Requirement r) {
        return new RequisiteEvaluator(s, true, MembershipResolver.NONE).evaluate(r);
    }

    private static AcademicSnapshot taken(CourseRecord... records) {
        return AcademicSnapshot.builder().taken(records).build();
    }

    private static String reason(EvaluationNode n) {
        return n.getReason().orElse("");
    }

    private static MembershipResolver rows(Map<Integer, Status> statuses) {
        return new MembershipResolver() {
            @Override
            public Match onList(String listName, CourseRecord record) {
                return Match.MAYBE;
            }

            @Override
            public Match inRowPool(int rowNumber, CourseRecord record) {
                return Match.MAYBE;
            }

            @Override
            public Optional<Status> rowStatus(int rowNumber) {
                return Optional.ofNullable(statuses.get(rowNumber));
            }
        };
    }

    private static CreditsReq breadth(double amount, Integer... categories) {
        return new CreditsReq(amount, null, null, CourseFilter.builder().breadth(List.of(categories)).build(), false);
    }

    private static AverageReq average(int minGrade, Double amount) {
        return new AverageReq(minGrade, amount, false, CSC, List.of(), false);
    }

    private static EvaluationNode profile(String attr, Object value, Object fact) {
        AcademicSnapshot s = fact == null
                ? AcademicSnapshot.builder().build()
                : AcademicSnapshot.builder().profileFact(attr, fact).build();

        return prereq(s, new ProfileReq(attr, value, attr + " " + value, false));
    }

    @Test
    void constructorRejectsNulls() {
        AcademicSnapshot s = taken();

        assertThrows(NullPointerException.class, () -> new RequisiteEvaluator(null, false, MembershipResolver.NONE));
        assertThrows(NullPointerException.class, () -> new RequisiteEvaluator(s, false, null));
    }

    @Test
    void andNeedsEveryItem() {
        Requirement r = and(req("CSC148H1"), req("MAT137Y1"));

        EvaluationNode met = prereq(taken(record("CSC148H1"), record("MAT137Y1")), r);
        assertEquals(MET, met.getStatus());
        assertEquals(2, met.getChildren().size());
        assertEquals(Set.of("CSC148H1", "MAT137Y1"), met.allCounted());

        assertEquals(NOT_MET, prereq(taken(record("CSC148H1")), r).getStatus());
    }

    @Test
    void orNeedsAnyItem() {
        Requirement r = or(req("MAT137Y1"), req("MAT157Y1"));

        assertEquals(MET, prereq(taken(record("MAT157Y1")), r).getStatus());
        assertEquals(NOT_MET, prereq(taken(), r).getStatus());
    }

    @Test
    void chooseCountsMetItems() {
        Requirement r = choose(2, req("CSC148H1"), req("CSC165H1"), req("CSC207H1"));

        EvaluationNode met = prereq(taken(record("CSC148H1"), record("CSC207H1")), r);
        assertEquals(MET, met.getStatus());
        assertEquals("2 of 2 met", reason(met));

        EvaluationNode notMet = prereq(taken(record("CSC148H1")), r);
        assertEquals(NOT_MET, notMet.getStatus());
        assertEquals("1 of 2 met", reason(notMet));
    }

    @Test
    void notChecksWholeTranscript() {
        EvaluationNode n = new RequisiteEvaluator(taken(), taken(record("CSC108H1")), false, MembershipResolver.NONE)
                .evaluate(not(req("CSC108H1")));

        assertEquals(NOT_MET, n.getStatus());
    }

    @Test
    void notNegates() {
        Requirement r = not(req("CSC148H1"));

        EvaluationNode notMet = prereq(taken(record("CSC148H1")), r);
        assertEquals(NOT_MET, notMet.getStatus());
        assertEquals("can't have CSC148H1", reason(notMet));

        EvaluationNode met = prereq(taken(), r);
        assertEquals(MET, met.getStatus());
        assertTrue(met.getReason().isEmpty());
    }

    @Test
    void courseMet() {
        EvaluationNode n = prereq(taken(record("CSC148H1")), req("CSC148H1"));

        assertEquals(MET, n.getStatus());
        assertEquals(List.of("CSC148H1"), n.getCounted());
        assertTrue(n.getReason().isEmpty());
    }

    @Test
    void courseNotTaken() {
        EvaluationNode n = prereq(taken(), req("CSC148H1"));

        assertEquals(NOT_MET, n.getStatus());
        assertEquals("CSC148H1 not in your plan", reason(n));
        assertTrue(n.getCounted().isEmpty());
    }

    @Test
    void reasonsMentionTheTerm() {
        AcademicSnapshot s = AcademicSnapshot.builder().asOf(AcademicTerm.winter(2027)).build();

        assertEquals("CSC148H1 not completed before Winter 2027", reason(prereq(s, req("CSC148H1"))));
    }

    @Test
    void courseGradeHighEnough() {
        assertEquals(MET, prereq(taken(record("CSC148H1", 70)), req("CSC148H1", 70)).getStatus());
    }

    @Test
    void courseGradeTooLow() {
        EvaluationNode n = prereq(taken(record("CSC148H1", 65)), req("CSC148H1", 70));

        assertEquals(NOT_MET, n.getStatus());
        assertEquals("needs at least 70% in CSC148H1; you have 65%", reason(n));
        assertEquals(List.of("CSC148H1"), n.getCounted());
    }

    @Test
    void courseGradeUnknownIsReview() {
        EvaluationNode n = prereq(taken(record("CSC148H1")), req("CSC148H1", 70));

        assertEquals(REVIEW, n.getStatus());
        assertEquals("needs at least 70% in CSC148H1; no final grade yet", reason(n));
    }

    @Test
    void concurrentCourseCountsOnlyWhenAllowed() {
        AcademicSnapshot s = AcademicSnapshot.builder().concurrent(record("CSC148H1")).build();

        assertEquals(NOT_MET, prereq(s, req("CSC148H1")).getStatus());
        assertEquals(MET, prereq(s, concurrentReq("CSC148H1")).getStatus());
        assertEquals(MET, coreq(s, req("CSC148H1")).getStatus());
        assertEquals(MET, coreq(s, concurrentReq("CSC148H1")).getStatus());
    }

    @Test
    void nestedConcurrentOnlyAffectsItsSubtree() {
        Requirement r = and(req("CSC148H1"), concurrentReq("MAT137Y1"));

        AcademicSnapshot aTakenBConcurrent = AcademicSnapshot.builder()
                .taken(record("CSC148H1")).concurrent(record("MAT137Y1")).build();
        assertEquals(MET, prereq(aTakenBConcurrent, r).getStatus());

        AcademicSnapshot aConcurrentBTaken = AcademicSnapshot.builder()
                .concurrent(record("CSC148H1")).taken(record("MAT137Y1")).build();
        assertEquals(NOT_MET, prereq(aConcurrentBTaken, r).getStatus());
    }

    @Test
    void concurrentCombinatorAppliesToItsChildren() {
        Requirement r = and(new OrReq(List.of(req("CSC148H1")), true), req("MAT137Y1"));
        AcademicSnapshot s = AcademicSnapshot.builder().concurrent(record("CSC148H1"), record("MAT137Y1")).build();

        EvaluationNode n = prereq(s, r);

        assertEquals(MET, n.getChildren().get(0).getStatus());
        assertEquals(NOT_MET, n.getChildren().get(1).getStatus());
    }

    @Test
    void creditsMet() {
        AcademicSnapshot s = taken(record("CSC148H1"), record("MAT137Y1"), record("CSC207H1"));

        EvaluationNode n = prereq(s, creditsIn(1.0, "CSC"));

        assertEquals(MET, n.getStatus());
        assertEquals(List.of("CSC148H1", "CSC207H1"), n.getCounted());
        assertTrue(n.getReason().isEmpty());
    }

    @Test
    void creditsNotMet() {
        EvaluationNode n = prereq(taken(record("CSC148H1")), creditsIn(1.0, "CSC"));

        assertEquals(NOT_MET, n.getStatus());
        assertEquals("0.5 of 1.0 credits", reason(n));
    }

    @Test
    void creditsReviewWithMaybes() {
        AcademicSnapshot s = taken(record("CSC148H1"), record("CSC207H1"));

        EvaluationNode n = prereq(s, new CreditsReq(1.0, null, null, GROUP_1, false));

        assertEquals(REVIEW, n.getStatus());
        assertEquals("0.0 of 1.0 credits", reason(n));
        assertEquals(List.of("CSC148H1", "CSC207H1"), n.getCounted());
    }

    @Test
    void creditsCappedByMaxAmount() {
        AcademicSnapshot s = taken(record("CSC148H1"), record("CSC207H1"));

        EvaluationNode n = prereq(s, new CreditsReq(1.0, 0.5, null, CSC, false));

        assertEquals(NOT_MET, n.getStatus());
        assertEquals("0.5 of 1.0 credits", reason(n));
    }

    @Test
    void creditsWeightOverridesCourseWeight() {
        EvaluationNode n = prereq(taken(record("CSC148H1")), new CreditsReq(1.0, null, 1.0, CSC, false));

        assertEquals(MET, n.getStatus());
    }

    @Test
    void creditsFloatingPointSums() {
        AcademicSnapshot s = taken(record("CSC108H1"), record("CSC148H1"), record("CSC165H1"), record("CSC207H1"),
                record("CSC209H1"), record("CSC236H1"), record("CSC258H1"), record("CSC263H1"), record("CSC343H1"),
                record("CSC369H1"));

        EvaluationNode n = prereq(s, new CreditsReq(1.0, null, 0.1, CSC, false));

        assertEquals(MET, n.getStatus());
    }

    @Test
    void creditsCountConcurrentOnlyWhenAllowed() {
        AcademicSnapshot s = AcademicSnapshot.builder()
                .taken(record("CSC148H1")).concurrent(record("CSC207H1")).build();

        assertEquals(NOT_MET, prereq(s, creditsIn(1.0, "CSC")).getStatus());
        assertEquals(MET, coreq(s, creditsIn(1.0, "CSC")).getStatus());
    }

    @Test
    void twoCategoryCourseIsReviewForOneCategory() {
        EvaluationNode n = prereq(taken(ANT100), breadth(1.0, 3));

        assertEquals(REVIEW, n.getStatus());
        assertEquals("0.5 of 1.0 credits", reason(n));
        assertEquals(List.of("ANT100Y1"), n.getCounted());
    }

    @Test
    void twoCategoryCourseHalfIsMet() {
        assertEquals(MET, prereq(taken(ANT100), breadth(0.5, 4)).getStatus());
    }

    @Test
    void filterCoveringBothCategoriesCountsFully() {
        assertEquals(MET, prereq(taken(ANT100), breadth(1.0, 3, 4)).getStatus());
    }

    @Test
    void singleCategoryCourseUnchanged() {
        CourseRecord r = record(withBreadth("MAT137Y1", BreadthCategory.PHYSICAL_MATHEMATICAL), 75);

        assertEquals(MET, prereq(taken(r), breadth(1.0, 5)).getStatus());
    }

    @Test
    void averageMet() {
        AcademicSnapshot s = taken(record("CSC148H1", 80), record("CSC207H1", 70), record("MAT137Y1", 10));

        EvaluationNode n = prereq(s, average(75, null));

        assertEquals(MET, n.getStatus());
        assertEquals(List.of("CSC148H1", "CSC207H1"), n.getCounted());
        assertTrue(n.getReason().isEmpty());
    }

    @Test
    void averageIsWeightedByCredits() {
        AcademicSnapshot s = taken(record("CSC148H1", 90), record("CSC199Y1", 60));

        EvaluationNode n = prereq(s, average(71, null));

        assertEquals(NOT_MET, n.getStatus());
        assertEquals("average 70.0%, needs 71%", reason(n));
    }

    @Test
    void averageReasonRoundsToOneDecimal() {
        AcademicSnapshot s = taken(record("CSC148H1", 80), record("CSC207H1", 60), record("CSC236H1", 65));

        assertEquals("average 68.3%, needs 70%", reason(prereq(s, average(70, null))));
    }

    @Test
    void unspecifiedTransferMayCountAsCourse() {
        EvaluationNode n = prereq(taken(MAT1), req("MAT135H1"));

        assertEquals(REVIEW, n.getStatus());
        assertEquals("MAT1** may count as MAT135H1 with the department's permission", reason(n));
        assertEquals(List.of("MAT1**"), n.getCounted());
    }

    @Test
    void unspecifiedTransferCreditsUseItsWeight() {
        assertEquals(REVIEW, prereq(taken(MAT1), creditsIn(2.0, "MAT")).getStatus());
        assertEquals(MET, prereq(taken(MAT1), credits(2.0, CourseFilter.any())).getStatus());
    }

    @Test
    void averageBestTakesHighestGrades() {
        AverageReq r = new AverageReq(85, 1.0, true, CSC, List.of(), false);
        AcademicSnapshot s = taken(record("CSC236H1", 50), record("CSC148H1", 90), record("CSC207H1", 80));

        EvaluationNode n = prereq(s, r);

        assertEquals(MET, n.getStatus());
        assertEquals(List.of("CSC148H1", "CSC207H1"), n.getCounted());
    }

    @Test
    void averageBestCountsPartOfLastCourse() {
        AverageReq r = new AverageReq(76, 1.0, true, CSC, List.of(), false);
        AcademicSnapshot s = taken(record("CSC148H1", 90), record("CSC199Y1", 60));

        assertEquals("average 75.0%, needs 76%", reason(prereq(s, r)));
    }

    @Test
    void averageBestMetDespiteUnknownGrade() {
        AverageReq r = new AverageReq(85, 0.5, true, CSC, List.of(), false);

        assertEquals(MET, prereq(taken(record("CSC148H1", 90), record("CSC207H1")), r).getStatus());
    }

    @Test
    void averageBestShortWithUnknownGradeIsReview() {
        AverageReq r = new AverageReq(85, 1.0, true, CSC, List.of(), false);

        EvaluationNode n = prereq(taken(record("CSC148H1", 90), record("CSC207H1")), r);

        assertEquals(REVIEW, n.getStatus());
        assertEquals("no final grade yet for CSC207H1", reason(n));
    }

    @Test
    void averageBestWithoutAmountAveragesAll() {
        AverageReq r = new AverageReq(80, null, true, CSC, List.of(), false);

        AcademicSnapshot s = taken(record("CSC148H1", 90), record("CSC207H1", 60));

        assertEquals("average 75.0%, needs 80%", reason(prereq(s, r)));
    }

    @Test
    void averageUnknownGradeCanLowerPlainAverage() {
        EvaluationNode n = prereq(taken(record("CSC148H1", 90), record("CSC207H1")), average(70, null));

        assertEquals(REVIEW, n.getStatus());
        assertEquals("no final grade yet for CSC207H1", reason(n));
    }

    @Test
    void averageItemsCoverEachItem() {
        AverageReq r = new AverageReq(70, 1.0, false, CourseFilter.any(),
                List.of(creditsIn(0.5, "CSC"), creditsIn(0.5, "MAT")), false);

        EvaluationNode n = prereq(taken(record("CSC148H1", 80), record("MAT135H1", 60)), r);

        assertEquals(MET, n.getStatus());
        assertEquals(List.of("CSC148H1", "MAT135H1"), n.getCounted());
    }

    @Test
    void averageItemsNeedEveryItem() {
        AverageReq r = new AverageReq(70, 1.0, false, CourseFilter.any(),
                List.of(creditsIn(0.5, "CSC"), creditsIn(0.5, "MAT")), false);

        EvaluationNode n = prereq(taken(record("CSC148H1", 80)), r);

        assertEquals(NOT_MET, n.getStatus());
        assertEquals("0.0 of 0.5 credits for average", reason(n));
    }

    @Test
    void averageItemsDontShareCourses() {
        AverageReq r = new AverageReq(70, 1.0, true, CourseFilter.any(),
                List.of(creditsIn(0.5, "CSC"), creditsIn(0.5, "CSC")), false);

        assertEquals(NOT_MET, prereq(taken(record("CSC148H1", 90)), r).getStatus());
    }

    @Test
    void averageItemsPickBestOption() {
        AverageReq r = new AverageReq(80, 0.5, false, CourseFilter.any(),
                List.of(or(creditsIn(0.5, "CSC"), creditsIn(0.5, "MAT"))), false);

        EvaluationNode n = prereq(taken(record("CSC148H1", 50), record("MAT135H1", 90)), r);

        assertEquals(MET, n.getStatus());
        assertEquals(List.of("MAT135H1"), n.getCounted());
    }

    @Test
    void averageItemsPickBestCombination() {
        AverageReq r = new AverageReq(85, 1.0, true, CourseFilter.any(),
                List.of(choose(2, creditsIn(0.5, "CSC"), creditsIn(0.5, "MAT"), creditsIn(0.5, "PHY"))), false);
        AcademicSnapshot s = taken(record("CSC148H1", 60), record("MAT135H1", 90), record("PHY131H1", 80));

        EvaluationNode n = prereq(s, r);

        assertEquals(MET, n.getStatus());
        assertEquals(List.of("MAT135H1", "PHY131H1"), n.getCounted());
    }

    @Test
    void averageItemsChoosingTooManyIsNotMet() {
        AverageReq r = new AverageReq(70, 1.0, false, CourseFilter.any(),
                List.of(choose(2, creditsIn(0.5, "CSC"))), false);

        assertEquals(NOT_MET, prereq(taken(record("CSC148H1", 90)), r).getStatus());
    }

    @Test
    void averageOverOtherItemsIsReview() {
        for (Requirement item : List.of(req("CSC148H1"), or(req("CSC148H1")), choose(1, req("CSC148H1")))) {
            AverageReq r = new AverageReq(70, null, false, CourseFilter.any(), List.of(item), false);

            EvaluationNode n = prereq(taken(record("CSC148H1", 90)), r);

            assertEquals(REVIEW, n.getStatus());
            assertEquals("average over these courses; check manually", reason(n));
        }
    }

    @Test
    void averageUnknownGradeIsReview() {
        EvaluationNode n = prereq(taken(record("CSC148H1")), average(70, null));

        assertEquals(REVIEW, n.getStatus());
        assertEquals("no final grade yet for CSC148H1", reason(n));
    }

    @Test
    void averageMaybeMatchIsReview() {
        AverageReq r = new AverageReq(70, null, false, GROUP_1, List.of(), false);

        EvaluationNode n = prereq(taken(record("CSC148H1", 90)), r);

        assertEquals(REVIEW, n.getStatus());
        assertEquals("can't tell if CSC148H1 counts", reason(n));
    }

    @Test
    void averageWithNoMatchesIsNotMet() {
        EvaluationNode n = prereq(taken(record("MAT137Y1", 90)), average(70, null));

        assertEquals(NOT_MET, n.getStatus());
        assertEquals("no matching courses", reason(n));
    }

    @Test
    void averageNeedsEnoughCredits() {
        EvaluationNode tooFew = prereq(taken(record("CSC148H1", 90)), average(70, 1.0));
        assertEquals(NOT_MET, tooFew.getStatus());
        assertEquals("0.5 of 1.0 credits for average", reason(tooFew));

        AcademicSnapshot enough = taken(record("CSC148H1", 90), record("CSC207H1", 80));
        assertEquals(MET, prereq(enough, average(70, 1.0)).getStatus());
    }

    @Test
    void gpaMet() {
        AcademicSnapshot s = AcademicSnapshot.builder().cgpa(3.0).build();

        assertEquals(MET, prereq(s, new GpaReq(2.0, null, false)).getStatus());
    }

    @Test
    void gpaNotMet() {
        AcademicSnapshot s = AcademicSnapshot.builder().cgpa(1.5).build();

        EvaluationNode n = prereq(s, new GpaReq(2.0, null, false));

        assertEquals(NOT_MET, n.getStatus());
        assertEquals("needs a CGPA of 2.0; you have 1.5", reason(n));
    }

    @Test
    void gpaUnknownIsReview() {
        assertEquals(REVIEW, prereq(taken(), new GpaReq(2.0, null, false)).getStatus());
    }

    @Test
    void gpaScopeIsReview() {
        AcademicSnapshot s = AcademicSnapshot.builder().cgpa(4.0).build();

        EvaluationNode n = prereq(s, new GpaReq(2.0, "APM and MAT courses", false));

        assertEquals(REVIEW, n.getStatus());
        assertEquals("GPA in APM and MAT courses; check manually", reason(n));
    }

    @Test
    void programEnrolledMet() {
        AcademicSnapshot s = AcademicSnapshot.builder().enrolledProgram("ASMAJ1689").build();

        assertEquals(MET, prereq(s, programReq("ASSPE1689", "ASMAJ1689")).getStatus());
    }

    @Test
    void programIntendedIsReview() {
        AcademicSnapshot s = AcademicSnapshot.builder().intendedProgram("ASMAJ1689").build();

        EvaluationNode n = prereq(s, programReq("ASMAJ1689"));

        assertEquals(REVIEW, n.getStatus());
        assertEquals("you intend to enrol in ASMAJ1689", reason(n));
    }

    @Test
    void programNotMet() {
        AcademicSnapshot s = AcademicSnapshot.builder().enrolledProgram("ASMAJ0001").build();

        EvaluationNode n = prereq(s, programReq("ASMAJ1689"));

        assertEquals(NOT_MET, n.getStatus());
        assertEquals("needs ASMAJ1689", reason(n));
    }

    @Test
    void requirementOutsideCompletionIsReview() {
        EvaluationNode n = prereq(taken(), new RowRefReq(List.of(1), false));

        assertEquals(REVIEW, n.getStatus());
        assertEquals("checked only in program completion", reason(n));
    }

    @Test
    void requirementMetWhenAnyRowMet() {
        RequisiteEvaluator e = new RequisiteEvaluator(taken(), false, rows(Map.of(1, NOT_MET, 2, MET)));

        EvaluationNode n = e.evaluate(new RowRefReq(List.of(1, 2), false));

        assertEquals(MET, n.getStatus());
        assertTrue(n.getReason().isEmpty());
    }

    @Test
    void requirementNotMet() {
        RequisiteEvaluator e = new RequisiteEvaluator(taken(), false, rows(Map.of(1, NOT_MET)));

        EvaluationNode n = e.evaluate(new RowRefReq(List.of(1), false));

        assertEquals(NOT_MET, n.getStatus());
        assertEquals("needs requirement [1]", reason(n));
    }

    @Test
    void profileByIsReview() {
        EvaluationNode n = profile("apply_by", "2027-01-15", "2027-01-01");

        assertEquals(REVIEW, n.getStatus());
        assertEquals("apply_by 2027-01-15", reason(n));
    }

    @Test
    void profileMissingIsReview() {
        assertEquals(REVIEW, profile("min_year", 2, null).getStatus());
    }

    @Test
    void profileMinMet() {
        assertEquals(MET, profile("min_year", 2, 2).getStatus());
        assertEquals(MET, profile("min_year", 2, 3).getStatus());
    }

    @Test
    void profileMinNotMet() {
        EvaluationNode n = profile("min_year", 2, 1);

        assertEquals(NOT_MET, n.getStatus());
        assertEquals("min_year 2", reason(n));
    }

    @Test
    void profileMax() {
        assertEquals(MET, profile("max_year", 3, 3).getStatus());
        assertEquals(NOT_MET, profile("max_year", 3, 4).getStatus());
    }

    @Test
    void profileHas() {
        assertEquals(MET, profile("has_permission", true, true).getStatus());
        assertEquals(NOT_MET, profile("has_permission", true, false).getStatus());
        assertEquals(NOT_MET, profile("has_permission", true, "yes").getStatus());
    }

    @Test
    void profileEquals() {
        assertEquals(MET, profile("faculty", "ASC", "ASC").getStatus());
        assertEquals(NOT_MET, profile("faculty", "ASC", "APSC").getStatus());
    }

    @Test
    void textIsReview() {
        EvaluationNode n = prereq(taken(), text("Permission of the instructor"));

        assertEquals(REVIEW, n.getStatus());
        assertEquals("Permission of the instructor", reason(n));
    }
}
