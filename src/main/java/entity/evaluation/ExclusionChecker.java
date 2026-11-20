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
        List<String> reasons = new ArrayList<>();
        List<String> blocking = new ArrayList<>();

        for (ExclusionItem item : course.getExclusions().get().getItems()) {
            if (item.isText()) {
                review = true;
                reasons.add(item.getText().get());
                continue;
            }

            List<String> codes = matches(item, course.getCode(), snapshot);

            if (codes.isEmpty()) {
                continue;
            }

            if (item.hasNote()) {
                review = true;
                reasons.add("may be excluded by " + String.join(" and ", codes) + " " + item.getNote().get());
            } else if (item.isConcurrent()) {
                excluded = true;
                blocking.addAll(codes);
                reasons.add("can't be taken at the same time as " + String.join(" and ", codes));
            } else {
                excluded = true;
                blocking.addAll(codes);
                reasons.add("excluded by " + String.join(" and ", codes));
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
