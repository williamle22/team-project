package app;

import data_access.catalog.FileCatalogCache;
import data_access.catalog.HttpCatalogDownloader;
import data_access.catalog.LiveCatalogRepository;
import data_access.catalog.SqliteCatalogLoader;
import data_access.plan.InMemoryPlanRepository;
import entity.plan.Plan;
import interface_adapter.dgx.AcademicPlannerPresenter;
import interface_adapter.dgx.AcademicPlannerViewModel;
import interface_adapter.dgx.AddCourseToPlanController;
import interface_adapter.dgx.CourseSearchViewModel;
import interface_adapter.sync_catalog.SyncCatalogController;
import interface_adapter.sync_catalog.SyncCatalogPresenter;
import interface_adapter.sync_catalog.SyncCatalogViewModel;
import use_case.sync_catalog.SyncCatalogInputBoundary;
import use_case.sync_catalog.SyncCatalogInteractor;
import use_case.add_course_to_plan.AddCourseToPlanInputBoundary;
import use_case.add_course_to_plan.AddCourseToPlanInteractor;
import view.CatalogStatusBar;
import view.dgx.AcademicPlannerPanel;
import view.dgx.CourseSearchPanel;
import view.dgx.DgxPane;

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

    public AppBuilder addDgxPane() {
        AcademicPlannerViewModel academicPlannerViewModel = new AcademicPlannerViewModel();
        CourseSearchViewModel courseSearchViewModel = new CourseSearchViewModel();

        Plan plan = TemporaryPlanStub.createAndSave(plans);

        AcademicPlannerPresenter plannerPresenter = new AcademicPlannerPresenter(academicPlannerViewModel);
        plannerPresenter.presentPlan(plan, "Drag a course result into a term to add it to your plan.");
        AddCourseToPlanInputBoundary addCourseInteractor = new AddCourseToPlanInteractor(
                plans, catalog, plannerPresenter);
        AddCourseToPlanController addCourseController =
                new AddCourseToPlanController(plan.getId(), addCourseInteractor);

        DgxPane dgxPane = new DgxPane(
            new CourseSearchPanel(courseSearchViewModel),
            new AcademicPlannerPanel(academicPlannerViewModel, addCourseController)
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
