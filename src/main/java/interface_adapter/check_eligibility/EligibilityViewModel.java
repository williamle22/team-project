package interface_adapter.check_eligibility;

import interface_adapter.ViewModel;

public final class EligibilityViewModel extends ViewModel<EligibilityState> {
    public EligibilityViewModel() {
        super("eligibility");
        setState(new EligibilityState());
    }
}
