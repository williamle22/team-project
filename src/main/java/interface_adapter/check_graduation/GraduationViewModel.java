package interface_adapter.check_graduation;

import interface_adapter.ViewModel;

public final class GraduationViewModel extends ViewModel<GraduationState> {
    public GraduationViewModel() {
        super("graduation");
        setState(new GraduationState());
    }
}
