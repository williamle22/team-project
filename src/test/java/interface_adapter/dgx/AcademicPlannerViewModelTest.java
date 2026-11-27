package interface_adapter.dgx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AcademicPlannerViewModelTest {
    @Test
    void startsWithEmptyPlannerState() {
        AcademicPlannerViewModel viewModel = new AcademicPlannerViewModel();

        assertEquals("academic planner", viewModel.getViewName());
        assertEquals(0, viewModel.getState().getTerms().size());
        assertEquals("", viewModel.getState().getMessage());
    }
}
