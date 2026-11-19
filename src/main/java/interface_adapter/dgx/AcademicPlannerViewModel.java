package interface_adapter.dgx;

import interface_adapter.ViewModel;

public class AcademicPlannerViewModel extends ViewModel<AcademicPlannerState> {
    public AcademicPlannerViewModel() {
        super("academic planner");
        setState(new AcademicPlannerState());
    }
}
