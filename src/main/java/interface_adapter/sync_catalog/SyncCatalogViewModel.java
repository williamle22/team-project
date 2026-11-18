package interface_adapter.sync_catalog;

import interface_adapter.ViewModel;

public final class SyncCatalogViewModel extends ViewModel<SyncCatalogState> {
    public SyncCatalogViewModel() {
        super("catalog status");
        setState(new SyncCatalogState());
    }
}
