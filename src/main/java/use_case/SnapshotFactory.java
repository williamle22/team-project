package use_case;

import entity.catalog.Course;
import entity.evaluation.AcademicSnapshot;
import entity.evaluation.CourseRecord;
import entity.grades.LetterGrade;
import entity.plan.CreditWeights;
import entity.plan.GpaCalculator;
import entity.plan.Plan;
import entity.plan.PlannedCourse;
import entity.plan.TransferCredit;
import entity.term.AcademicTerm;
import entity.term.Placement;

import java.util.*;

public final class SnapshotFactory {
    private final CatalogReader catalog;
    private final GpaCalculator gpa;

    public SnapshotFactory(CatalogReader catalog, GpaCalculator gpa) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.gpa = Objects.requireNonNull(gpa, "gpa");
    }

    public AcademicSnapshot forCourse(Plan plan, PlannedCourse target) {
        Placement placement = target.getPlacement();
        AcademicTerm start = placement.startTerm();
        List<PlannedCourse> earlier = new ArrayList<>();
        List<PlannedCourse> sameTime = new ArrayList<>();

        for (PlannedCourse c : plan.getCourses()) {
            if (failed(c)) {
                continue;
            }

            if (c.getPlacement().endTerm().isBefore(start)) {
                earlier.add(c);
            } else if (!Collections.disjoint(c.getPlacement().terms(), placement.terms())) {
                sameTime.add(c);
            }
        }

        Map<String, CourseRecord> taken = taken(plan, earlier);
        Map<String, CourseRecord> concurrent = records(sameTime);

        concurrent.keySet().removeAll(taken.keySet());
        taken.remove(target.getCode());
        concurrent.remove(target.getCode());

        return builder(plan, taken, plan.coursesEndedBefore(start), gpa.cgpaBefore(plan, start))
                .concurrent(concurrent.values())
                .asOf(start)
                .build();
    }

    public AcademicSnapshot wholePlan(Plan plan) {
        List<PlannedCourse> notFailed = new ArrayList<>();

        for (PlannedCourse c : plan.getCourses()) {
            if (!failed(c)) {
                notFailed.add(c);
            }
        }

        return builder(plan, taken(plan, notFailed), plan.getCourses(), gpa.cgpa(plan)).build();
    }

    private Map<String, CourseRecord> taken(Plan plan, List<PlannedCourse> courses) {
        Map<String, CourseRecord> taken = records(courses);

        for (TransferCredit t : plan.getTransferCredits()) {
            catalog.findCourse(t.getCode())
                    .ifPresent(course -> keepBest(taken, new CourseRecord(course, null, true, false)));
        }

        return taken;
    }

    private Map<String, CourseRecord> records(List<PlannedCourse> courses) {
        Map<String, CourseRecord> records = new LinkedHashMap<>();

        for (PlannedCourse c : courses) {
            Optional<Course> course = catalog.findCourse(c.getCode());

            if (course.isPresent()) {
                Integer grade = c.getGrade().getFinalGrade().orElse(null);
                keepBest(records, new CourseRecord(course.get(), grade, false, c.isCrNcr()));
            }
        }

        return records;
    }

    private static void keepBest(Map<String, CourseRecord> records, CourseRecord r) {
        CourseRecord current = records.get(r.getCode());

        if (current == null || r.getGrade().orElse(-1) > current.getGrade().orElse(-1)) {
            records.put(r.getCode(), r);
        }
    }

    private static AcademicSnapshot.Builder builder(Plan plan, Map<String, CourseRecord> taken,
                                                    List<PlannedCourse> completed, OptionalDouble cgpa) {
        AcademicSnapshot.Builder b = AcademicSnapshot.builder()
                .taken(taken.values())
                .profileFact("min_year_of_study", yearOfStudy(credits(plan, completed)))
                .profileFact("has_transfer_credits", !plan.getTransferCredits().isEmpty());

        cgpa.ifPresent(b::cgpa);

        for (String code : plan.enrolledPrograms()) {
            b.enrolledProgram(code);
        }

        for (String code : plan.intendedPrograms()) {
            b.intendedProgram(code);
        }

        return b;
    }

    private static double credits(Plan plan, List<PlannedCourse> completed) {
        Map<String, Double> weights = new HashMap<>();

        for (PlannedCourse c : completed) {
            if (passed(c)) {
                weights.put(c.getCode(), CreditWeights.FROM_CODE.weightOf(c.getCode()));
            }
        }

        for (TransferCredit t : plan.getTransferCredits()) {
            weights.putIfAbsent(t.getCode(), t.getWeight());
        }

        double total = 0;

        for (double w : weights.values()) {
            total += w;
        }

        return total;
    }

    private static int yearOfStudy(double credits) {
        if (credits < 4.0) {
            return 1;
        } else if (credits < 9.0) {
            return 2;
        } else if (credits < 14.0) {
            return 3;
        }

        return 4;
    }

    private static boolean passed(PlannedCourse c) {
        Optional<Integer> grade = c.getGrade().getFinalGrade();
        return grade.isPresent() && LetterGrade.fromPercent(grade.get()).isPassing();
    }

    private static boolean failed(PlannedCourse c) {
        Optional<Integer> grade = c.getGrade().getFinalGrade();
        return grade.isPresent() && !LetterGrade.fromPercent(grade.get()).isPassing();
    }
}
