package interface_adapter.dgx;

import entity.term.AcademicTerm;
import entity.term.Season;
import use_case.add_course_to_plan.AddCourseToPlanInputBoundary;
import use_case.add_course_to_plan.AddCourseToPlanInputData;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Translates a dropped course and term target into an add-course request. */
public final class AddCourseToPlanController {
    private final UUID planId;
    private final AddCourseToPlanInputBoundary interactor;

    public AddCourseToPlanController(UUID planId, AddCourseToPlanInputBoundary interactor) {
        this.planId = Objects.requireNonNull(planId, "planId");
        this.interactor = Objects.requireNonNull(interactor, "interactor");
    }

    public void execute(String courseCode, int year, String season) {
        Season targetSeason = Season.valueOf(season.toUpperCase(Locale.ROOT));
        interactor.execute(new AddCourseToPlanInputData(planId, courseCode, new AcademicTerm(targetSeason, year)));
    }
}
