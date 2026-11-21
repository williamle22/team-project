package interface_adapter.check_eligibility;

import entity.evaluation.Status;
import use_case.check_eligibility.CheckEligibilityOutputBoundary;
import use_case.check_eligibility.CheckEligibilityOutputData;
import use_case.check_eligibility.CourseEligibility;
import use_case.check_eligibility.EligibilityLine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CheckEligibilityPresenter implements CheckEligibilityOutputBoundary {
    private static final Pattern COURSE_CODE = Pattern.compile("[A-Z]{3}\\d{3}[HY]\\d");

    private final EligibilityViewModel viewModel;

    public CheckEligibilityPresenter(EligibilityViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(CheckEligibilityOutputData data) {
        Map<UUID, CourseEligibilityView> courses = new LinkedHashMap<>();

        for (CourseEligibility c : data.getCourses().values()) {
            courses.put(c.getPlannedCourseId(), toView(c));
        }

        EligibilityState state = new EligibilityState();
        state.setPlanId(data.getPlanId());
        state.setCourses(courses);
        state.setPlanWarnings(data.getPlanWarnings());

        update(state);
    }

    @Override
    public void prepareFailView(String message) {
        EligibilityState state = new EligibilityState();
        state.setError(message);

        update(state);
    }

    private void update(EligibilityState state) {
        viewModel.setState(state);
        viewModel.firePropertyChanged();
    }

    private static CourseEligibilityView toView(CourseEligibility c) {
        List<EligibilityLineView> lines = toViews(c.getSections());

        return new CourseEligibilityView(c.getPlannedCourseId(), c.getCode(), status(c.getOverall()),
                summary(c.getOverall(), lines), lines, c.getWarnings());
    }

    private static List<EligibilityLineView> toViews(List<EligibilityLine> lines) {
        return lines.stream()
                .map(l -> new EligibilityLineView(l.getText(), status(l.getStatus()), reason(l),
                        toViews(l.getChildren())))
                .collect(Collectors.toList());
    }

    private static String reason(EligibilityLine line) {
        String reason = line.getReason().orElse("");
        return reason.equalsIgnoreCase(line.getText()) ? "" : reason;
    }

    private static String status(Status s) {
        return s.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private static String summary(Status overall, List<EligibilityLineView> lines) {
        if (overall == Status.MET) {
            return "Requirements met";
        }

        EligibilityLineView line = first(lines, status(overall));

        if (line == null) {
            return overall == Status.NOT_MET ? "Requirements not met" : "Needs review";
        }

        String detail = line.getReason().isEmpty() ? line.getText() : line.getReason();

        if (overall == Status.REVIEW) {
            return "Needs review: " + decapitalize(detail);
        }

        Matcher code = COURSE_CODE.matcher(line.getText());

        if (code.lookingAt() && line.getReason().startsWith(code.group())) {
            return "Missing " + line.getText();
        }

        return capitalize(detail);
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }

        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String decapitalize(String s) {
        if (s.length() < 2 || !Character.isLowerCase(s.charAt(1))) {
            return s;
        }

        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private static EligibilityLineView first(List<EligibilityLineView> lines, String status) {
        for (EligibilityLineView line : lines) {
            if (line.getStatus().equals(status)) {
                EligibilityLineView deeper = first(line.getChildren(), status);
                return deeper == null ? line : deeper;
            }
        }

        return null;
    }
}
