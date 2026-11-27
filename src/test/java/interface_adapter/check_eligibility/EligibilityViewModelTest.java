package interface_adapter.check_eligibility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EligibilityViewModelTest {

    @Test
    void startsWithEmptyState() {
        EligibilityViewModel viewModel = new EligibilityViewModel();

        assertEquals("eligibility", viewModel.getViewName());
        assertEquals(new EligibilityState(), viewModel.getState());
    }
}
