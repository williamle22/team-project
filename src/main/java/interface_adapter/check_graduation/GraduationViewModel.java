package interface_adapter.check_graduation;

import interface_adapter.ViewModel;

/** The view model for the graduation panel. */
public final class GraduationViewModel extends ViewModel<GraduationState> {
    public GraduationViewModel() {
        super("graduation");
        setState(new GraduationState());
    }
}
