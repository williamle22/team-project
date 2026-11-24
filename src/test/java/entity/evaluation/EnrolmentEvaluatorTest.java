package entity.evaluation;

import entity.catalog.Enrolment;
import entity.catalog.EnrolmentKind;
import entity.requirement.ProgramReq;
import entity.requirement.Requirement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.programReq;
import static testutil.CatalogFixtures.record;
import static testutil.CatalogFixtures.req;
import static testutil.CatalogFixtures.text;

class EnrolmentEvaluatorTest {
    private static final AcademicSnapshot EMPTY = AcademicSnapshot.builder().build();
    private static final AcademicSnapshot TOOK_CSC148 = AcademicSnapshot.builder().taken(record("CSC148H1")).build();

    private final EnrolmentEvaluator evaluator = new EnrolmentEvaluator();

    private static Enrolment enrolment(EnrolmentKind kind, Requirement req, List<ProgramReq> exclusions,
                                       boolean firstRoundOnly) {
        return new Enrolment(kind, req, exclusions, firstRoundOnly, List.of("calendar note"), "raw");
    }

    private static Enrolment enrolment(EnrolmentKind kind, Requirement req) {
        return enrolment(kind, req, List.of(), false);
    }

    @Test
    void nullEnrolmentIsReview() {
        EnrolmentResult r = evaluator.evaluate(null, EMPTY);

        assertEquals(REVIEW, r.getStatus());
        assertEquals("no enrolment information", r.getReason());
        assertTrue(r.getDetail().isEmpty());
        assertEquals(List.of(), r.getNotes());
    }

    @Test
    void suspendedNotAdmitting() {
        EnrolmentResult r = evaluator.evaluate(enrolment(EnrolmentKind.SUSPENDED, null), TOOK_CSC148);

        assertEquals(NOT_MET, r.getStatus());
        assertEquals("not admitting students", r.getReason());
        assertTrue(r.getDetail().isEmpty());
        assertEquals(List.of("calendar note"), r.getNotes());
    }

    @Test
    void openMet() {
        EnrolmentResult r = evaluator.evaluate(enrolment(EnrolmentKind.OPEN, req("CSC148H1")), TOOK_CSC148);

        assertEquals(MET, r.getStatus());
        assertEquals("meets the enrolment requirements", r.getReason());
        assertEquals(MET, r.getDetail().orElseThrow().getStatus());
    }

    @Test
    void openNotMet() {
        EnrolmentResult r = evaluator.evaluate(enrolment(EnrolmentKind.OPEN, req("CSC148H1")), EMPTY);

        assertEquals(NOT_MET, r.getStatus());
        assertEquals("doesn't meet the enrolment requirements", r.getReason());
        assertEquals(NOT_MET, r.getDetail().orElseThrow().getStatus());
    }

    @Test
    void openReview() {
        EnrolmentResult r = evaluator.evaluate(enrolment(EnrolmentKind.OPEN, text("permission of the department")),
                EMPTY);

        assertEquals(REVIEW, r.getStatus());
        assertEquals("some enrolment requirements need review", r.getReason());
    }

    @Test
    void limitedMetSaysApply() {
        EnrolmentResult r = evaluator.evaluate(enrolment(EnrolmentKind.LIMITED, req("CSC148H1")), TOOK_CSC148);

        assertEquals(MET, r.getStatus());
        assertEquals("eligible to apply; admission not guaranteed", r.getReason());
    }

    @Test
    void limitedNotMet() {
        EnrolmentResult r = evaluator.evaluate(enrolment(EnrolmentKind.LIMITED, req("CSC148H1")), EMPTY);

        assertEquals(NOT_MET, r.getStatus());
        assertEquals("doesn't meet the enrolment requirements", r.getReason());
    }

    @Test
    void focusNeedsParentProgram() {
        AcademicSnapshot enrolled = AcademicSnapshot.builder().enrolledProgram("ASMAJ1689").build();

        EnrolmentResult met = evaluator.evaluate(enrolment(EnrolmentKind.FOCUS, programReq("ASMAJ1689")), enrolled);
        EnrolmentResult notMet = evaluator.evaluate(enrolment(EnrolmentKind.FOCUS, programReq("ASMAJ1689")), EMPTY);

        assertEquals(MET, met.getStatus());
        assertEquals(NOT_MET, notMet.getStatus());
    }

    @Test
    void concurrentCourseDoesntCount() {
        AcademicSnapshot inProgress = AcademicSnapshot.builder().concurrent(record("CSC148H1")).build();

        EnrolmentResult r = evaluator.evaluate(enrolment(EnrolmentKind.OPEN, req("CSC148H1")), inProgress);

        assertEquals(NOT_MET, r.getStatus());
    }

    @Test
    void excludedProgramBlocks() {
        AcademicSnapshot snapshot = AcademicSnapshot.builder().taken(record("CSC148H1"))
                .enrolledProgram("ASMAJ2").build();
        Enrolment e = enrolment(EnrolmentKind.OPEN, req("CSC148H1"), List.of(programReq("ASMAJ1", "ASMAJ2")), false);

        EnrolmentResult r = evaluator.evaluate(e, snapshot);

        assertEquals(NOT_MET, r.getStatus());
        assertEquals("can't be combined with ASMAJ2", r.getReason());
        assertEquals(MET, r.getDetail().orElseThrow().getStatus());
    }

    @Test
    void excludedProgramNotEnrolledIgnored() {
        AcademicSnapshot snapshot = AcademicSnapshot.builder().taken(record("CSC148H1"))
                .enrolledProgram("ASMAJ3").build();
        Enrolment e = enrolment(EnrolmentKind.OPEN, req("CSC148H1"), List.of(programReq("ASMAJ1", "ASMAJ2")), false);

        EnrolmentResult r = evaluator.evaluate(e, snapshot);

        assertEquals(MET, r.getStatus());
    }

    @Test
    void firstRoundNote() {
        Enrolment e = enrolment(EnrolmentKind.OPEN, req("CSC148H1"), List.of(), true);

        EnrolmentResult r = evaluator.evaluate(e, TOOK_CSC148);

        assertEquals(List.of("calendar note", "requests are only considered in the first round"), r.getNotes());
    }
}
