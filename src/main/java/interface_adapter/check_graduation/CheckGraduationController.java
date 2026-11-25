package interface_adapter.check_graduation;

import use_case.check_graduation.CheckGraduationInputBoundary;
import use_case.check_graduation.CheckGraduationInputData;

import java.util.UUID;

public final class CheckGraduationController {
    private final CheckGraduationInputBoundary interactor;

    public CheckGraduationController(CheckGraduationInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(UUID planId, boolean projected) {
        interactor.execute(new CheckGraduationInputData(planId, projected));
    }
}
