package interface_adapter.check_graduation;

import use_case.check_graduation.CheckGraduationInputBoundary;
import use_case.check_graduation.CheckGraduationInputData;

import java.util.UUID;

/** Starts Use Case 10. The check runs on the calling thread. */
public final class CheckGraduationController {
    private final CheckGraduationInputBoundary interactor;

    public CheckGraduationController(CheckGraduationInputBoundary interactor) {
        this.interactor = interactor;
    }

    /** Checks the plan, counting the whole plan if {@code projected} is true, or only completed courses otherwise. */
    public void execute(UUID planId, boolean projected) {
        interactor.execute(new CheckGraduationInputData(planId, projected));
    }
}
