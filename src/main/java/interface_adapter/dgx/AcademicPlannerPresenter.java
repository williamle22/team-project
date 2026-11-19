package interface_adapter.dgx;

import entity.plan.Plan;
import entity.plan.PlannedCourse;
import entity.term.AcademicTerm;
import entity.term.Season;
import interface_adapter.dgx.AcademicPlannerState.CourseRow;
import interface_adapter.dgx.AcademicPlannerState.TermRow;
import use_case.add_course_to_plan.AddCourseToPlanOutputBoundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Maps plan data into the planner's display-only state. */
public final class AcademicPlannerPresenter implements AddCourseToPlanOutputBoundary {
    private final AcademicPlannerViewModel viewModel;

    public AcademicPlannerPresenter(AcademicPlannerViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
    }

    public void presentPlan(Plan plan, String message) {
        List<TermRow> rows = new ArrayList<>();
        for (AcademicTerm term : plan.getReversedTerms()) {
            if (term.getSeason() == Season.SUMMER) {
                continue;
            }
            List<CourseRow> courses = new ArrayList<>();
            for (PlannedCourse course : plan.coursesIn(term)) {
                courses.add(new CourseRow(course.getCode()));
            }
            rows.add(new TermRow(term.getYear(), titleCase(term.getSeason()), courses));
        }
        viewModel.setState(new AcademicPlannerState(rows, message));
        viewModel.firePropertyChanged();
    }

    @Override
    public void prepareSuccessView(Plan plan, String message) {
        presentPlan(plan, message);
    }

    @Override
    public void prepareFailView(String message) {
        AcademicPlannerState current = viewModel.getState();
        viewModel.setState(new AcademicPlannerState(current.getTerms(), message));
        viewModel.firePropertyChanged();
    }

    private static String titleCase(Season season) {
        String lower = season.name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
