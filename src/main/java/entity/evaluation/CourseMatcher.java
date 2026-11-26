package entity.evaluation;

import entity.catalog.Course;
import entity.requirement.CourseFilter;

import java.util.ArrayList;
import java.util.List;

/** Checks a course against a {@link CourseFilter}. */
public final class CourseMatcher {
    private CourseMatcher() {}

    /**
     * Returns {@code YES} if {@code r} passes every part of {@code f}, {@code NO} if it fails any part, and {@code
     * MAYBE} otherwise. A minimum grade with no final grade yet gives {@code MAYBE}. {@code lists} answers the course
     * list and completion row parts.
     */
    public static Match matches(CourseFilter f, CourseRecord r, MembershipResolver lists) {
        Course c = r.getCourse();
        List<Match> checks = new ArrayList<>();

        if (!f.matchesAnyCourse()
                && !(f.getCourses().contains(c.getCode()) || f.getCourses().stream().anyMatch(r::couldBe)
                || f.getDesignators().contains(c.getDesignator()))) {
            checks.add(Match.NO);
        } else {
            checks.add(Match.YES);
        }

        if (!f.getLevels().isEmpty() && !f.getLevels().contains(c.getLevel())) {
            checks.add(Match.NO);
        } else {
            checks.add(Match.YES);
        }

        if (!f.getBreadth().isEmpty() && !r.isUnspecified()
                && c.getBreadth().stream().noneMatch(b -> f.getBreadth().contains(b.getNumber()))) {
            checks.add(Match.NO);
        } else {
            checks.add(Match.YES);
        }

        if (f.getExclude().contains(c.getCode())) {
            checks.add(Match.NO);
        } else {
            checks.add(Match.YES);
        }

        if (f.getExcludeDesignators().contains(c.getDesignator())) {
            checks.add(Match.NO);
        } else {
            checks.add(Match.YES);
        }

        if (f.getMaxCourseAmount().isPresent() && r.getWeight() > f.getMaxCourseAmount().get()) {
            checks.add(Match.NO);
        } else {
            checks.add(Match.YES);
        }

        if (f.getMinGrade().isPresent() && r.getGrade().isPresent() && r.getGrade().get() < f.getMinGrade().get()) {
            checks.add(Match.NO);
        } else if (f.getMinGrade().isPresent() && r.getGrade().isEmpty()) {
            checks.add(Match.MAYBE);
        } else {
            checks.add(Match.YES);
        }

        if (f.isTransfer() && !r.isTransfer()) {
            checks.add(Match.NO);
        } else {
            checks.add(Match.YES);
        }

        if (f.isCrNcr() && !r.isCrNcr()) {
            checks.add(Match.NO);
        } else {
            checks.add(Match.YES);
        }

        if (!f.getLists().isEmpty()) {
            List<Match> onAny = new ArrayList<>();

            for (String name : f.getLists()) {
                onAny.add(lists.onList(name, r));
            }

            checks.add(Match.anyOf(onAny));
        }

        if (!f.getRequirements().isEmpty()) {
            List<Match> inAny = new ArrayList<>();

            for (int n : f.getRequirements()) {
                inAny.add(lists.inRowPool(n, r));
            }

            checks.add(Match.anyOf(inAny));
        }

        Match m = Match.allOf(checks);
        boolean specific = !f.matchesAnyCourse() || !f.getBreadth().isEmpty();

        return m == Match.YES && r.isUnspecified() && specific ? Match.MAYBE : m;
    }
}
