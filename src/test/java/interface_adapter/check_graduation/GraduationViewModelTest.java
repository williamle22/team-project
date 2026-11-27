package interface_adapter.check_graduation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraduationViewModelTest {

    @Test
    void startsWithEmptyState() {
        GraduationViewModel viewModel = new GraduationViewModel();

        assertEquals("graduation", viewModel.getViewName());
        assertEquals(new GraduationState(), viewModel.getState());
    }
}
