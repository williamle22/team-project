package interface_adapter.sync_catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SyncCatalogViewModelTest {

    @Test
    void startsWithBlankState() {
        SyncCatalogViewModel viewModel = new SyncCatalogViewModel();
        assertEquals("catalog status", viewModel.getViewName());
        assertEquals(new SyncCatalogState(), viewModel.getState());
    }
}
