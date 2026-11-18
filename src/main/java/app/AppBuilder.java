package app;

import data_access.catalog.FileCatalogCache;
import data_access.catalog.HttpCatalogDownloader;
import data_access.catalog.LiveCatalogRepository;
import data_access.catalog.SqliteCatalogLoader;
import interface_adapter.sync_catalog.SyncCatalogController;
import interface_adapter.sync_catalog.SyncCatalogPresenter;
import interface_adapter.sync_catalog.SyncCatalogViewModel;
import use_case.sync_catalog.SyncCatalogInputBoundary;
import use_case.sync_catalog.SyncCatalogInteractor;
import view.CatalogStatusBar;

import javax.swing.*;
import java.awt.*;

// Add one add...() method per use case/view, like addCatalogSync().
// Pass `catalog` to every use case that reads the catalog so they all see the synced data.
public final class AppBuilder {
    private final AppConfig config = AppConfig.load();
    private final LiveCatalogRepository catalog = new LiveCatalogRepository();
    // CENTER is free for main views (CardLayout + ViewManagerModel)
    private final JPanel root = new JPanel(new BorderLayout());

    private SyncCatalogController syncCatalogController;

    public AppBuilder addCatalogSync() {
        SyncCatalogViewModel viewModel = new SyncCatalogViewModel();
        SyncCatalogPresenter presenter = new SyncCatalogPresenter(viewModel);
        SyncCatalogInputBoundary interactor = new SyncCatalogInteractor(
                new HttpCatalogDownloader(config.dgxUrl(), config.dataDirectory()),
                new FileCatalogCache(config.dataDirectory(), new SqliteCatalogLoader()),
                catalog, presenter);

        syncCatalogController = new SyncCatalogController(interactor, presenter);

        root.add(new CatalogStatusBar(viewModel, syncCatalogController), BorderLayout.SOUTH);

        return this;
    }

    public JFrame build() {
        JFrame frame = new JFrame("Degree Explorer");

        frame.setContentPane(root);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        return frame;
    }

    public void startCatalogSync() {
        syncCatalogController.execute();
    }
}
