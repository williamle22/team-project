package use_case.check_eligibility;

import entity.catalog.Course;
import entity.evaluation.*;
import entity.plan.Plan;
import entity.plan.PlannedCourse;
import entity.plan.TransferCredit;
import entity.requirement.*;
import entity.term.Placement;
import use_case.CatalogReader;
import use_case.CurrentAcademicYear;
import use_case.PlanReader;
import use_case.SnapshotFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Use Case 8: checks each planned course with an {@link EligibilityEvaluator}, and turns the results into display
 * lines. A course missing from the catalog gets {@code REVIEW}. Plan warnings flag unknown codes, retakes, unspecified
 * transfer credits, and pairs of courses in the same term that exclude each other.
 */
public final class CheckEligibilityInteractor implements CheckEligibilityInputBoundary {
    private final PlanReader plans;
    private final CatalogReader catalog;
    private final SnapshotFactory snapshots;
    private final CurrentAcademicYear currentYear;
    private final EligibilityEvaluator evaluator;
    private final CheckEligibilityOutputBoundary presenter;
    private final RequirementPrinter printer = new RequirementPrinter();

    public CheckEligibilityInteractor(PlanReader plans, CatalogReader catalog, SnapshotFactory snapshots,
                                      CurrentAcademicYear currentYear, EligibilityEvaluator evaluator,
                                      CheckEligibilityOutputBoundary presenter) {
        this.plans = plans;
        this.catalog = catalog;
        this.snapshots = snapshots;
        this.currentYear = currentYear;
        this.evaluator = evaluator;
        this.presenter = presenter;
    }

    @Override
    public void execute(CheckEligibilityInputData input) {
        if (!catalog.isLoaded()) {
            presenter.prepareFailView("Course data is still loading.");
            return;
        }

        Optional<Plan> found = plans.findPlan(input.getPlanId());

        if (found.isEmpty()) {
            presenter.prepareFailView("That plan no longer exists.");
            return;
        }

        Plan plan = found.get();
        Map<UUID, CourseEligibility> courses = new LinkedHashMap<>();
        List<Course> resolved = new ArrayList<>();

        for (PlannedCourse c : plan.getCourses()) {
            Optional<Course> course = catalog.findCourse(c.getCode());

            if (course.isEmpty()) {
                courses.put(c.getId(), unknown(c));
                continue;
            }

            resolved.add(course.get());
            courses.put(c.getId(), check(plan, c, course.get()));
        }

        presenter.prepareSuccessView(new CheckEligibilityOutputData(plan.getId(), courses,
                planWarnings(plan, resolved)));
    }

    private static CourseEligibility unknown(PlannedCourse c) {
        EligibilityLine line = new EligibilityLine("Not in the current calendar", Status.REVIEW, null, List.of());

        return new CourseEligibility(c.getId(), c.getCode(), c.getPlacement().toString(), Status.REVIEW,
                List.of(line), List.of());
    }

    private CourseEligibility check(Plan plan, PlannedCourse c, Course course) {
        Placement placement = c.getPlacement();
        EligibilityResult result = evaluator.evaluate(course, placement, snapshots.forCourse(plan, c),
                currentYear.isCurrent(placement));

        List<EligibilityLine> sections = new ArrayList<>();
        sections.add(section("Prerequisites", result.getPrerequisites()));
        sections.add(section("Corequisites", result.getCorequisites()));
        sections.add(exclusions(result.getExclusions()));

        if (result.getOffering() == OfferingStatus.OFFERED) {
            sections.add(new EligibilityLine("Offered in " + placement, Status.MET, null, List.of()));
        } else if (result.getOffering() == OfferingStatus.NOT_OFFERED) {
            sections.add(new EligibilityLine("Not offered in " + placement, Status.NOT_MET, null, List.of()));
        }

        List<String> warnings = new ArrayList<>();

        for (String w : result.getWarnings()) {
            warnings.add(capitalize(w));
        }


        return new CourseEligibility(c.getId(), c.getCode(), placement.toString(), result.getOverall(), sections,
                warnings);
    }

    private EligibilityLine section(String title, EvaluationNode node) {
        return new EligibilityLine(title, node.getStatus(), null, List.of(toLine(node)));
    }

    private static EligibilityLine exclusions(ExclusionOutcome outcome) {
        List<EligibilityLine> reasons = new ArrayList<>();

        for (ExclusionReason reason : outcome.getReasons()) {
            reasons.add(new EligibilityLine(capitalize(reason.getText()), reason.getResult().toStatus(), null,
                    List.of()));
        }

        return new EligibilityLine("Exclusions", outcome.getResult().toStatus(), null, reasons);
    }

    private EligibilityLine toLine(EvaluationNode node) {
        if (node.getRequirement().isEmpty()) {
            return new EligibilityLine(capitalize(node.getReason().get()), node.getStatus(), null, List.of());
        }

        Requirement r = node.getRequirement().get();
        List<EvaluationNode> children = node.getChildren();

        if ((r instanceof AndReq || r instanceof OrReq) && children.size() == 1 && !r.isConcurrent()) {
            return toLine(children.get(0));
        }

        String text;

        if (r instanceof AndReq) {
            text = "All of";
        } else if (r instanceof OrReq) {
            text = "One of";
        } else if (r instanceof ChooseReq) {
            text = ((ChooseReq) r).getCount() + " of";
        } else if (r instanceof NotReq) {
            text = "None of";
        } else {
            return new EligibilityLine(capitalize(printer.print(r)), node.getStatus(), node.getReason().orElse(null),
                    List.of());
        }

        if (r.isConcurrent()) {
            text += " (may be taken concurrently)";
        }

        List<EligibilityLine> lines = children.stream().map(this::toLine).collect(Collectors.toList());

        return new EligibilityLine(text + ":", node.getStatus(), node.getReason().orElse(null), lines);
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }

        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private List<String> planWarnings(Plan plan, List<Course> resolved) {
        List<String> warnings = new ArrayList<>();
        Set<String> codes = new LinkedHashSet<>();

        for (PlannedCourse c : plan.getCourses()) {
            codes.add(c.getCode());
        }

        for (TransferCredit t : plan.getTransferCredits()) {
            if (!t.getCode().contains("*")) {
                codes.add(t.getCode());
            }
        }

        for (String code : codes) {
            if (catalog.findCourse(code).isEmpty()) {
                warnings.add(code + " isn't in the current calendar");
            }
        }

        Map<String, Integer> counts = new LinkedHashMap<>();

        for (PlannedCourse c : plan.getCourses()) {
            counts.merge(c.getCode(), 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() > 1) {
                warnings.add(e.getKey() + " appears " + e.getValue() + " times (retake?)");
            }
        }

        Set<String> seen = new HashSet<>();

        for (Map.Entry<String, Set<String>> e : ExclusionChecker.conflictsWithin(resolved).entrySet()) {
            String a = e.getKey();

            for (String b : e.getValue()) {
                String pair = a.compareTo(b) < 0 ? a + " " + b : b + " " + a;

                if (sameTime(plan, a, b) && seen.add(pair)) {
                    warnings.add(a + " and " + b + " exclude each other");
                }
            }
        }

        for (TransferCredit t : plan.getTransferCredits()) {
            if (t.getCode().contains("*") && !t.getCode().startsWith("USP")) {
                warnings.add(t.getCode() + " is an unspecified transfer credit; it counts toward a requirement "
                        + "only with the department's permission");
            }
        }

        return warnings;
    }

    private static boolean sameTime(Plan plan, String a, String b) {
        for (PlannedCourse x : plan.getCourses()) {
            for (PlannedCourse y : plan.getCourses()) {
                if (x.getCode().equals(a) && y.getCode().equals(b)
                        && !Collections.disjoint(x.getPlacement().terms(), y.getPlacement().terms())) {
                    return true;
                }
            }
        }

        return false;
    }
}
