package app;

import data_access.catalog.FileCatalogCache;
import data_access.catalog.HttpCatalogDownloader;
import data_access.catalog.LiveCatalogRepository;
import data_access.catalog.SqliteCatalogLoader;
import interface_adapter.search_courses.SearchCoursesController;
import interface_adapter.search_courses.SearchCoursesPresenter;
import interface_adapter.search_courses.SearchCoursesViewModel;
import interface_adapter.sync_catalog.SyncCatalogController;
import interface_adapter.sync_catalog.SyncCatalogPresenter;
import interface_adapter.sync_catalog.SyncCatalogViewModel;
import use_case.search_courses.SearchCoursesInteractor;
import use_case.sync_catalog.SyncCatalogInputBoundary;
import use_case.sync_catalog.SyncCatalogInteractor;
import view.CatalogStatusBar;
import view.SearchPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Builds the main window. Each {@code add} method connects one use case and its view. Pass {@code catalog} to every use
 * case that reads the catalog, so they all see the synced data.
 */
public final class AppBuilder {
    private final AppConfig config = AppConfig.load();
    private final LiveCatalogRepository catalog = new LiveCatalogRepository();
    // CENTER is free for main views (CardLayout + ViewManagerModel)
    private final JPanel root = new JPanel(new BorderLayout());
    private final SyncCatalogViewModel syncCatalogViewModel = new SyncCatalogViewModel();

    private SyncCatalogController syncCatalogController;

    public AppBuilder addCatalogSync() {
        SyncCatalogPresenter presenter = new SyncCatalogPresenter(syncCatalogViewModel);
        SyncCatalogInputBoundary interactor = new SyncCatalogInteractor(
                new HttpCatalogDownloader(config.dgxUrl(), config.dataDirectory()),
                new FileCatalogCache(config.dataDirectory(), new SqliteCatalogLoader()),
                catalog, presenter);

        syncCatalogController = new SyncCatalogController(interactor, presenter);

        root.add(new CatalogStatusBar(syncCatalogViewModel, syncCatalogController), BorderLayout.SOUTH);

        return this;
    }

    public AppBuilder addSearch() {
        SearchCoursesViewModel viewModel = new SearchCoursesViewModel();
        SearchCoursesPresenter presenter = new SearchCoursesPresenter(viewModel);
        SearchCoursesInteractor interactor = new SearchCoursesInteractor(catalog, presenter);
        SearchCoursesController controller = new SearchCoursesController(interactor);
        SearchPanel panel = new SearchPanel(viewModel, controller);

        syncCatalogViewModel.addPropertyChangeListener(e -> {
            if (!syncCatalogViewModel.getState().isLoading()) {
                panel.refresh();
            }
        });

        root.add(panel, BorderLayout.WEST);

        return this;
    }

    /** Returns the main window. The caller makes it visible. */
    public JFrame build() {
        JFrame frame = new JFrame("Degree Explorer");

        frame.setContentPane(root);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        return frame;
    }

    /**
     * Starts UC1 in the background. Call it after the window is visible, so the status bar can show progress.
     */
    public void startCatalogSync() {
        syncCatalogController.execute();
    }
}
