package app;

import data_access.catalog.FileCatalogCache;
import data_access.catalog.HttpCatalogDownloader;
import data_access.catalog.LiveCatalogRepository;
import data_access.catalog.SqliteCatalogLoader;
import interface_adapter.search_courses.SearchCoursesController;
import interface_adapter.search_courses.SearchCoursesPresenter;
import interface_adapter.search_courses.SearchCoursesViewModel;
import data_access.plan.InMemoryPlanRepository;
import entity.plan.Plan;
import interface_adapter.dgx.AcademicPlannerPresenter;
import interface_adapter.dgx.AcademicPlannerViewModel;
import interface_adapter.dgx.AddCourseToPlanController;
import interface_adapter.dgx.AddTermToPlanController;
import interface_adapter.sync_catalog.SyncCatalogController;
import interface_adapter.sync_catalog.SyncCatalogPresenter;
import interface_adapter.sync_catalog.SyncCatalogViewModel;
import use_case.search_courses.SearchCoursesInteractor;
import use_case.sync_catalog.SyncCatalogInputBoundary;
import use_case.sync_catalog.SyncCatalogInteractor;
import use_case.add_course_to_plan.AddCourseToPlanInputBoundary;
import use_case.add_course_to_plan.AddCourseToPlanInteractor;
import use_case.add_term_to_plan.AddTermToPlanInputBoundary;
import use_case.add_term_to_plan.AddTermToPlanInteractor;
import view.CatalogStatusBar;
import view.dgx.AcademicPlannerPanel;
import view.dgx.DgxPane;
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
    private final InMemoryPlanRepository plans = new InMemoryPlanRepository();
    // CENTER is free for main views (CardLayout + ViewManagerModel)
    private final JPanel root = new JPanel(new BorderLayout());
    private final SyncCatalogViewModel syncCatalogViewModel = new SyncCatalogViewModel();

    private SyncCatalogController syncCatalogController;
    private SearchPanel searchPanel;

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
        searchPanel = new SearchPanel(viewModel, controller);

        syncCatalogViewModel.addPropertyChangeListener(e -> {
            if (!syncCatalogViewModel.getState().isLoading()) {
                searchPanel.refresh();
            }
        });

        return this;
    }

    public AppBuilder addDgxPane() {
        AcademicPlannerViewModel academicPlannerViewModel = new AcademicPlannerViewModel();
        Plan plan = TemporaryPlanStub.createAndSave(plans);

        AcademicPlannerPresenter plannerPresenter = new AcademicPlannerPresenter(academicPlannerViewModel);
        plannerPresenter.presentPlan(plan, "Drag a course result into a term to add it to your plan.");
        AddCourseToPlanInputBoundary addCourseInteractor = new AddCourseToPlanInteractor(
                plans, catalog, plannerPresenter);
        AddCourseToPlanController addCourseController =
                new AddCourseToPlanController(plan.getId(), addCourseInteractor);
        AddTermToPlanInputBoundary addTermInteractor = new AddTermToPlanInteractor(plans, plannerPresenter);
        AddTermToPlanController addTermController = new AddTermToPlanController(plan.getId(), addTermInteractor);

        DgxPane dgxPane = new DgxPane(
            searchPanel,
            new AcademicPlannerPanel(academicPlannerViewModel, addCourseController, addTermController)
        );

        root.add(dgxPane, BorderLayout.CENTER);

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
