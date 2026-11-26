package use_case.check_graduation;

import entity.catalog.Completion;
import entity.catalog.CompletionRow;
import entity.catalog.Enrolment;
import entity.catalog.Program;
import entity.degree.Degree;
import entity.evaluation.*;
import entity.plan.Plan;
import entity.plan.PlannedCourse;
import entity.plan.ProgramSelection;
import entity.plan.TransferCredit;
import entity.requirement.RequirementPrinter;
import use_case.CatalogReader;
import use_case.DegreeReader;
import use_case.PlanReader;
import use_case.SnapshotFactory;

import java.util.*;

/**
 * Use Case 10: checks the plan's degree with a {@link DegreeEvaluator}, and each of its programs with an
 * {@link EnrolmentEvaluator} and a {@link CompletionEvaluator}. It turns the results into display lines. The overall
 * status is the degree's: its rows and its program combination. A program missing from the catalog gets {@code REVIEW}
 * lines and doesn't count toward the combination. The interactor leaves out rows that don't apply to the student. Notes
 * flag courses that aren't in the current calendar, unspecified transfer credits, and courses that might count twice.
 */
public final class CheckGraduationInteractor implements CheckGraduationInputBoundary {
    private final PlanReader plans;
    private final CatalogReader catalog;
    private final DegreeReader degrees;
    private final SnapshotFactory snapshots;
    private final CheckGraduationOutputBoundary presenter;
    private final EnrolmentEvaluator enrolment = new EnrolmentEvaluator();
    private final CompletionEvaluator completion = new CompletionEvaluator();
    private final DegreeEvaluator degreeEvaluator = new DegreeEvaluator(completion);
    private final RequirementPrinter printer = new RequirementPrinter();

    public CheckGraduationInteractor(PlanReader plans, CatalogReader catalog, DegreeReader degrees,
                                     SnapshotFactory snapshots, CheckGraduationOutputBoundary presenter) {
        this.plans = plans;
        this.catalog = catalog;
        this.degrees = degrees;
        this.snapshots = snapshots;
        this.presenter = presenter;
    }

    @Override
    public void execute(CheckGraduationInputData input) {
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

        if (plan.getDegree().isEmpty()) {
            presenter.prepareFailView("Choose a degree for this plan.");
            return;
        }

        Degree degree = degrees.findDegree(plan.getDegree().get()).orElseThrow();
        AcademicSnapshot snapshot = input.isProjected() ? snapshots.wholePlan(plan) : snapshots.completedOnly(plan);
        List<ProgramProgress> progress = new ArrayList<>();
        List<ProgramSummary> summaries = new ArrayList<>();

        for (ProgramSelection s : plan.getPrograms()) {
            Optional<Program> program = catalog.findProgram(s.getCode());

            if (program.isEmpty()) {
                summaries.add(unknown(s.getCode()));
                continue;
            }

            ProgramProgress p = progress(program.get(), snapshot);
            progress.add(p);
            summaries.add(summary(program.get(), p));
        }

        DegreeProgress d = degreeEvaluator.evaluate(degree, snapshot, progress);
        Completion rules = degree.getCompletion();
        RowLine degreeLine = new RowLine("Degree", degree.getTitle(), d.getCompletion().getOverall(), null,
                List.of(), lines(rules, d.getCompletion(), rules.topLevelRows()));
        RowLine combination = new RowLine("Programs", degree.getProgramCombinations().describe(),
                d.getProgramCombination(), d.getProgramCombinationReason().orElse(null), List.of(), List.of());

        List<String> notes = new ArrayList<>(unplacedCredits(plan));
        notes.addAll(rules.getNotes());

        presenter.prepareSuccessView(new CheckGraduationOutputData(plan.getId(), input.isProjected(),
                d.getOverall(), degreeLine, combination, notes, summaries));
    }

    private static ProgramSummary unknown(String code) {
        return new ProgramSummary(code, code, unknownLine("Enrolment"), unknownLine("Completion"), List.of());
    }

    private static RowLine unknownLine(String label) {
        return new RowLine(label, "Not in the current calendar", Status.REVIEW, null, List.of(), List.of());
    }

    private ProgramProgress progress(Program program, AcademicSnapshot snapshot) {
        EnrolmentResult e = enrolment.evaluate(program.getEnrolment().orElse(null), snapshot);
        CompletionResult c = program.getCompletion()
                .map(rules -> completion.evaluate(rules, snapshot))
                .orElse(new CompletionResult(List.of(), Status.REVIEW));

        return new ProgramProgress(program.getCode(), program.getType(), e, c);
    }

    private ProgramSummary summary(Program program, ProgramProgress p) {
        EnrolmentResult e = p.getEnrolment();
        CompletionResult c = p.getCompletion();
        Optional<Completion> rules = program.getCompletion();
        RowLine enrolmentLine = new RowLine("Enrolment",
                program.getEnrolment().map(Enrolment::getRaw).orElse("Enrolment requirements"), e.getStatus(),
                e.getReason(), List.of(), List.of());
        RowLine completionLine = new RowLine("Completion", "Completion requirements", c.getOverall(),
                rules.isPresent() ? null : "no completion information", List.of(),
                rules.map(r -> lines(r, c, r.topLevelRows())).orElse(List.of()));
        List<String> notes = new ArrayList<>(e.getNotes());
        rules.ifPresent(r -> notes.addAll(r.getNotes()));

        return new ProgramSummary(program.getCode(), program.getTitle(), enrolmentLine, completionLine, notes);
    }

    private List<RowLine> lines(Completion rules, CompletionResult result, List<CompletionRow> rows) {
        List<RowLine> lines = new ArrayList<>();

        for (CompletionRow row : rows) {
            RowResult r = result.row(row.getNumber()).orElseThrow();

            if (r.isApplicable()) {
                lines.add(line(rules, result, r));
            }
        }

        return lines;
    }

    private RowLine line(Completion rules, CompletionResult result, RowResult r) {
        CompletionRow row = r.getRow();
        String text = row.getReq().map(printer::print).orElse("One of these options");
        String reason = r.getReason().or(() -> r.getDetail().flatMap(EvaluationNode::getReason)).orElse(null);

        return new RowLine("Requirement " + row.getNumber(), text, r.getStatus(), reason, r.getClaimed(),
                lines(rules, result, rules.childrenOf(row.getNumber())));
    }

    private List<String> unplacedCredits(Plan plan) {
        Set<String> codes = new LinkedHashSet<>();
        List<String> notes = new ArrayList<>();

        for (PlannedCourse c : plan.getCourses()) {
            codes.add(c.getCode());
        }

        for (TransferCredit t : plan.getTransferCredits()) {
            if (!t.getCode().contains("*")) {
                codes.add(t.getCode());
            } else if (!t.getCode().startsWith("USP")) {
                notes.add(t.getCode() + " is an unspecified transfer credit; it counts toward a requirement "
                        + "only with the department's permission");
            }
        }

        for (String code : codes) {
            if (catalog.findCourse(code).isEmpty()) {
                notes.add(code + " isn't in the current calendar, so it doesn't count toward any requirement");
            }
        }

        return notes;
    }
}
