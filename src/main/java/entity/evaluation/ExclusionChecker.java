package entity.evaluation;

import entity.catalog.Course;
import entity.catalog.ExclusionItem;

import java.util.*;
import java.util.stream.Collectors;

public final class ExclusionChecker {
    private ExclusionChecker() {}

    public static ExclusionOutcome check(Course course, AcademicSnapshot snapshot) {
        if (course.getExclusions().isEmpty()) {
            return new ExclusionOutcome(ExclusionResult.NOT_EXCLUDED, List.of(), List.of());
        }

        boolean excluded = false;
        boolean review = false;
        List<ExclusionReason> reasons = new ArrayList<>();
        List<String> blocking = new ArrayList<>();

        for (ExclusionItem item : course.getExclusions().get().getItems()) {
            if (item.isText()) {
                review = true;
                reasons.add(new ExclusionReason(item.getText().get(), ExclusionResult.REVIEW));
                continue;
            }

            List<String> codes = matches(item, course.getCode(), snapshot);

            if (codes.isEmpty()) {
                continue;
            }

            String joined = String.join(" and ", codes);

            if (item.hasNote()) {
                review = true;
                reasons.add(new ExclusionReason("may be excluded by " + joined + " " + item.getNote().get(),
                        ExclusionResult.REVIEW));
            } else if (item.isConcurrent()) {
                excluded = true;
                blocking.addAll(codes);
                reasons.add(new ExclusionReason("can't be taken at the same time as " + joined,
                        ExclusionResult.EXCLUDED));
            } else {
                excluded = true;
                blocking.addAll(codes);
                reasons.add(new ExclusionReason("excluded by " + joined, ExclusionResult.EXCLUDED));
            }
        }

        ExclusionResult result = excluded ? ExclusionResult.EXCLUDED
                : review ? ExclusionResult.REVIEW : ExclusionResult.NOT_EXCLUDED;

        return new ExclusionOutcome(result, reasons, blocking);
    }

    public static Map<String, Set<String>> conflictsWithin(Collection<Course> courses) {
        Set<String> codes = courses.stream().map(Course::getCode).collect(Collectors.toSet());
        Map<String, Set<String>> conflicts = new LinkedHashMap<>();

        for (Course c1 : courses) {
            if (c1.getExclusions().isEmpty()) {
                continue;
            }

            for (Course c2 : courses) {
                if (c2.getCode().equals(c1.getCode())) {
                    continue;
                }

                for (ExclusionItem item : c1.getExclusions().get().getItems()) {
                    if (item.isText() || item.hasNote() || item.isConcurrent()) {
                        continue;
                    }

                    if (item.isCourseSet()
                            ? item.getCourses().contains(c2.getCode()) && codes.containsAll(item.getCourses())
                            : filterMatches(item, c2)) {
                        conflicts.computeIfAbsent(c1.getCode(), k -> new LinkedHashSet<>()).add(c2.getCode());
                    }
                }
            }
        }

        return conflicts;
    }

    private static List<String> matches(ExclusionItem item, String self, AcademicSnapshot snapshot) {
        if (item.isFilter()) {
            List<String> codes = new ArrayList<>();

            for (CourseRecord r : snapshot.getTaken().values()) {
                if (!r.getCode().equals(self) && filterMatches(item, r.getCourse())) {
                    codes.add(r.getCode());
                }
            }

            return codes;
        }

        return (item.isConcurrent() ? snapshot.getConcurrent() : snapshot.getTaken())
                .keySet().containsAll(item.getCourses()) ? item.getCourses() : List.of();
    }

    private static boolean filterMatches(ExclusionItem item, Course c) {
        return item.getDesignators().contains(c.getDesignator())
                && (item.getLevels().isEmpty() || item.getLevels().contains(c.getLevel()))
                && !item.getExclude().contains(c.getCode());
    }
}
