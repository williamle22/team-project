package entity.evaluation;

import entity.catalog.Course;
import entity.catalog.Requisite;
import entity.requirement.Requirement;
import entity.term.Placement;

import java.util.List;
import java.util.Optional;

/** Checks one course's prerequisites, corequisites, exclusions, and offering. */
public final class EligibilityEvaluator {

    /**
     * Corequisites can be taken at the same time as the course. A prerequisite must come before it, unless the
     * prerequisite is marked concurrent. A course with no prerequisites or corequisites meets them. {@code
     * currentAcademicYear} sets whether to check the offering.
     */
    public EligibilityResult evaluate(Course course, Placement placement, AcademicSnapshot snapshot,
                                      boolean currentAcademicYear) {
        EvaluationNode prerequisites = requisite(course.getPrerequisites(), snapshot, false, "no prerequisites");
        EvaluationNode corequisites = requisite(course.getCorequisites(), snapshot, true, "no corequisites");
        ExclusionOutcome exclusions = ExclusionChecker.check(course, snapshot);
        OfferingStatus offering = OfferingCheck.check(course, placement, currentAcademicYear);

        List<String> warnings = offering == OfferingStatus.NOT_OFFERED
                ? List.of("not offered in " + placement) : List.of();

        return EligibilityResult.of(course.getCode(), prerequisites, corequisites, exclusions, offering, warnings);
    }

    private static EvaluationNode requisite(Optional<Requisite> requisite, AcademicSnapshot snapshot,
                                            boolean concurrentAllowed, String none) {
        Optional<Requirement> req = requisite.flatMap(Requisite::getReq);

        if (req.isEmpty()) {
            return EvaluationNode.none(none);
        }

        return new RequisiteEvaluator(snapshot, concurrentAllowed, MembershipResolver.NONE).evaluate(req.get());
    }
}
