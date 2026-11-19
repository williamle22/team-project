package interface_adapter.search_courses;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchCoursesViewModelTest {

    @Test
    void startsWithEmptyState() {
        SearchCoursesViewModel viewModel = new SearchCoursesViewModel();
        assertEquals("search courses", viewModel.getViewName());
        assertEquals(new SearchCoursesState(), viewModel.getState());
    }
}
