package interface_adapter.search_programs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchProgramsViewModelTest {

    @Test
    void startsWithEmptyState() {
        SearchProgramsViewModel viewModel = new SearchProgramsViewModel();
        assertEquals("search programs", viewModel.getViewName());
        assertEquals(new SearchProgramsState(), viewModel.getState());
    }
}
