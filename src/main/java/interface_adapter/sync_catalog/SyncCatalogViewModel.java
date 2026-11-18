package interface_adapter.sync_catalog;

import interface_adapter.ViewModel;

/** The view model for the catalog status bar. */
public final class SyncCatalogViewModel extends ViewModel<SyncCatalogState> {
    public SyncCatalogViewModel() {
        super("catalog status");
        setState(new SyncCatalogState());
    }
}
