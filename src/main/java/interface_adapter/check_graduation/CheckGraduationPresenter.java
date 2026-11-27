package interface_adapter.check_graduation;

import entity.evaluation.Status;
import use_case.check_graduation.CheckGraduationOutputBoundary;
import use_case.check_graduation.CheckGraduationOutputData;
import use_case.check_graduation.ProgramSummary;
import use_case.check_graduation.RowLine;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Turns Use Case 10's results into view lines and a one-line summary. It drops a reason that repeats its line's text,
 * and explains that a CGPA requirement stays unknown until the student enters final grades.
 */
public final class CheckGraduationPresenter implements CheckGraduationOutputBoundary {
    private static final String CGPA_UNKNOWN = "CGPA unknown";

    private final GraduationViewModel viewModel;

    public CheckGraduationPresenter(GraduationViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(CheckGraduationOutputData data) {
        GraduationState state = new GraduationState();
        state.setPlanId(data.getPlanId());
        state.setProjected(data.isProjected());
        state.setStatus(status(data.getOverall()));
        state.setSummary(summary(data.getOverall(), data.isProjected()));
        state.setDegree(lineView(data.getDegree()));
        state.setCombination(lineView(data.getCombination()));
        state.setNotes(data.getNotes());
        state.setPrograms(data.getPrograms().stream().map(CheckGraduationPresenter::programView)
                .collect(Collectors.toList()));

        update(state);
    }

    @Override
    public void prepareFailView(String message) {
        GraduationState state = new GraduationState();
        state.setError(message);

        update(state);
    }

    private void update(GraduationState state) {
        viewModel.setState(state);
        viewModel.firePropertyChanged();
    }

    private static ProgramView programView(ProgramSummary p) {
        return new ProgramView(p.getCode(), p.getTitle(), status(p.getCompletion().getStatus()),
                lineView(p.getEnrolment()), lineView(p.getCompletion()), p.getNotes());
    }

    private static GraduationLineView lineView(RowLine line) {
        return new GraduationLineView(line.getLabel(), line.getText(), status(line.getStatus()), reason(line),
                String.join(", ", line.getClaimed()),
                line.getChildren().stream().map(CheckGraduationPresenter::lineView).collect(Collectors.toList()));
    }

    private static String reason(RowLine line) {
        String reason = line.getReason().filter(r -> !r.equalsIgnoreCase(line.getText())).orElse("");

        if (reason.endsWith(CGPA_UNKNOWN)) {
            return reason + " until you enter final grades";
        }

        return reason;
    }

    private static String status(Status s) {
        return s.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private static String summary(Status overall, boolean projected) {
        if (overall == Status.REVIEW) {
            return "Some requirements need review";
        }

        if (overall == Status.NOT_MET) {
            return projected ? "This plan doesn't meet every requirement" : "Not ready to graduate yet";
        }

        return projected ? "This plan meets every requirement" : "Ready to graduate";
    }
}
