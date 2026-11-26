package use_case.add_course_to_plan;

import entity.catalog.Course;
import entity.catalog.Section;
import entity.plan.Plan;
import entity.term.AcademicTerm;
import entity.term.Placement;
import entity.term.Season;
import use_case.CatalogReader;
import use_case.PlanRepository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Coordinates adding a course to a plan and saving the updated plan. */
public final class AddCourseToPlanInteractor implements AddCourseToPlanInputBoundary {
    private final PlanRepository plans;
    private final CatalogReader catalog;
    private final AddCourseToPlanOutputBoundary presenter;

    public AddCourseToPlanInteractor(PlanRepository plans, CatalogReader catalog,
                                     AddCourseToPlanOutputBoundary presenter) {
        this.plans = Objects.requireNonNull(plans, "plans");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
    }

    @Override
    public void execute(AddCourseToPlanInputData input) {
        Objects.requireNonNull(input, "input");
        UUID planId = input.getPlanId();
        String courseCode = input.getCourseCode();
        AcademicTerm targetTerm = input.getTargetTerm();
        if (targetTerm == null || targetTerm.getSeason() == Season.SUMMER) {
            presenter.prepareFailView("Summer course drops aren't supported yet.");
            return;
        }

        Optional<Plan> storedPlan = plans.findPlan(planId);
        if (storedPlan.isEmpty()) {
            presenter.prepareFailView("The academic plan couldn't be found.");
            return;
        }

        Optional<Course> catalogCourse = catalog.findCourse(courseCode);
        if (catalogCourse.isEmpty()) {
            presenter.prepareFailView("Course " + courseCode + " isn't available in the loaded catalog.");
            return;
        }

        Plan plan = storedPlan.get();
        Placement placement = placementFor(catalogCourse.get(), targetTerm);
        try {
            plan.addCourse(catalogCourse.get().getCode(), placement);
        } catch (IllegalArgumentException e) {
            presenter.prepareFailView("Failed to add course " + courseCode + " to plan.");
            return;
        }

        plans.save(plan);
        presenter.prepareSuccessView(new AddCourseToPlanOutputData(plan,
                "Added " + catalogCourse.get().getCode() + " to " + targetTerm + "."));
    }

    private static Placement placementFor(Course course, AcademicTerm targetTerm) {
        if (course.getWeight() == 1.0) {
            int fallYear = targetTerm.getSeason() == Season.WINTER ? targetTerm.getYear() - 1 : targetTerm.getYear();
            return Placement.fallWinter(fallYear, Section.Y);
        }
        return Placement.halfCourseIn(targetTerm);
    }
}
