package entity.evaluation;

import entity.catalog.Course;
import entity.requirement.CourseFilter;

import java.util.ArrayList;
import java.util.List;

public final class CourseMatcher {
    private CourseMatcher() {}

    public static Match matches(CourseFilter f, CourseRecord r, MembershipResolver lists) {
        Course c = r.getCourse();
        List<Match> checks = new ArrayList<>();

        if (!f.matchesAnyCourse()
                && !(f.getCourses().contains(c.getCode()) || f.getDesignators().contains(c.getDesignator()))) {
            checks.add(Match.NO);
        } else {
            checks.add(Match.YES);
        }

        if (!f.getLevels().isEmpty() && !f.getLevels().contains(c.getLevel())) {
            checks.add(Match.NO);
        } else {
            checks.add(Match.YES);
        }

        if (!f.getBreadth().isEmpty()
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

        if (f.getMaxCourseAmount().isPresent() && c.getWeight() > f.getMaxCourseAmount().get()) {
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

        return Match.allOf(checks);
    }
}
