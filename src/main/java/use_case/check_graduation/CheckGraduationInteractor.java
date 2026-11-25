package use_case.check_graduation;

import entity.catalog.Completion;
import entity.catalog.CompletionRow;
import entity.catalog.Enrolment;
import entity.catalog.Program;
import entity.degree.Degree;
import entity.evaluation.*;
import entity.plan.Plan;
import entity.plan.ProgramSelection;
import entity.requirement.RequirementPrinter;
import use_case.CatalogReader;
import use_case.DegreeReader;
import use_case.PlanReader;
import use_case.SnapshotFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        List<String> notes = new ArrayList<>(rules.getNotes());
        notes.addAll(d.getCompletion().getWarnings());

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
                .orElse(new CompletionResult(List.of(), Status.REVIEW, List.of()));

        return new ProgramProgress(program.getCode(), program.getType(), e, c);
    }

    private ProgramSummary summary(Program program, ProgramProgress p) {
        EnrolmentResult e = p.getEnrolment();
        CompletionResult c = p.getCompletion();
        RowLine enrolmentLine = new RowLine("Enrolment",
                program.getEnrolment().map(Enrolment::getRaw).orElse("Enrolment requirements"), e.getStatus(),
                e.getReason(), List.of(), List.of());
        RowLine completionLine = program.getCompletion()
                .map(rules -> new RowLine("Completion", "Completion requirements", c.getOverall(), null, List.of(),
                        lines(rules, c, rules.topLevelRows())))
                .orElse(new RowLine("Completion", "Completion requirements", Status.REVIEW,
                        "no completion information", List.of(), List.of()));
        List<String> notes = new ArrayList<>(e.getNotes());
        notes.addAll(c.getWarnings());
        program.getCompletion().ifPresent(rules -> notes.addAll(rules.getNotes()));

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
}
