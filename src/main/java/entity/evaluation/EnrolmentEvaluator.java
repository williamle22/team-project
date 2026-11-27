package entity.evaluation;

import entity.catalog.Enrolment;
import entity.catalog.EnrolmentKind;
import entity.requirement.ProgramReq;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks whether a student can enrol in a program: its enrolment requirements, and the programs it can't be combined
 * with.
 */
public final class EnrolmentEvaluator {
    /**
     * Returns {@code REVIEW} if {@code enrolment} is {@code null}, because the calendar has no enrolment information. A
     * program that isn't admitting students, or that can't be combined with a program the student is enrolled in, is
     * {@code NOT_MET}. A limited-enrolment program whose requirements the student meets is {@code MET}, with a reason
     * saying that admission isn't guaranteed.
     */
    public EnrolmentResult evaluate(Enrolment enrolment, AcademicSnapshot snapshot) {
        if (enrolment == null) {
            return new EnrolmentResult(Status.REVIEW, "no enrolment information", null, List.of());
        }

        List<String> notes = new ArrayList<>(enrolment.getNotes());
        if (enrolment.isFirstRoundOnly()) {
            notes.add("requests are only considered in the first round");
        }

        if (enrolment.getKind() == EnrolmentKind.SUSPENDED) {
            return new EnrolmentResult(Status.NOT_MET, "not admitting students", null, notes);
        }

        EvaluationNode detail = new RequisiteEvaluator(snapshot, false, MembershipResolver.NONE)
                .evaluate(enrolment.getReq().orElseThrow());

        for (ProgramReq excluded : enrolment.getExclusions()) {
            for (String code : excluded.getPrograms()) {
                if (snapshot.getEnrolledPrograms().contains(code)) {
                    return new EnrolmentResult(Status.NOT_MET, "can't be combined with " + code, detail, notes);
                }
            }
        }

        Status status = detail.getStatus();
        return new EnrolmentResult(status, reason(enrolment.getKind(), status), detail, notes);
    }

    private static String reason(EnrolmentKind kind, Status status) {
        if (status == Status.NOT_MET) {
            return "doesn't meet the enrolment requirements";
        }

        if (status == Status.REVIEW) {
            return "some enrolment requirements need review";
        }

        if (kind == EnrolmentKind.LIMITED) {
            return "eligible to apply; admission not guaranteed";
        }

        return "meets the enrolment requirements";
    }
}
