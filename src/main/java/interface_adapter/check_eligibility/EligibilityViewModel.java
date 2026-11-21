package interface_adapter.check_eligibility;

import interface_adapter.ViewModel;

/** The view model for the eligibility views. */
public final class EligibilityViewModel extends ViewModel<EligibilityState> {
    public EligibilityViewModel() {
        super("eligibility");
        setState(new EligibilityState());
    }
}
