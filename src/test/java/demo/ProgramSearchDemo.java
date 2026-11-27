package demo;

import data_access.catalog.LiveCatalogRepository;
import data_access.catalog.SqliteCatalogLoader;
import interface_adapter.search_programs.ProgramResultRow;
import interface_adapter.search_programs.SearchProgramsController;
import interface_adapter.search_programs.SearchProgramsPresenter;
import interface_adapter.search_programs.SearchProgramsState;
import interface_adapter.search_programs.SearchProgramsViewModel;
import use_case.search_programs.SearchProgramsInteractor;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

public final class ProgramSearchDemo {
    private static final int SHOWN = 10;

    private final SearchProgramsViewModel viewModel = new SearchProgramsViewModel();
    private final SearchProgramsController controller;

    private ProgramSearchDemo(LiveCatalogRepository catalog) {
        controller = new SearchProgramsController(
                new SearchProgramsInteractor(catalog, new SearchProgramsPresenter(viewModel)));
    }

    private void search(String query, Set<String> types) {
        controller.execute(query, types);
        SearchProgramsState state = viewModel.getState();
        List<ProgramResultRow> rows = state.getResults();

        System.out.println();
        System.out.println("query \"" + query + "\", types " + (types.isEmpty() ? "any" : types));

        if (!state.getMessage().isEmpty()) {
            System.out.println("  " + state.getMessage());
        }

        for (ProgramResultRow row : rows.subList(0, Math.min(SHOWN, rows.size()))) {
            System.out.printf("  %-10s %-12s %s%n", row.getCode(), row.getType(), row.getTitle());
        }

        if (rows.size() > SHOWN) {
            System.out.println("  ... and " + (rows.size() - SHOWN) + " more (" + rows.size() + " total)");
        }
    }

    public static void main(String[] args) {
        LiveCatalogRepository catalog = new LiveCatalogRepository();
        ProgramSearchDemo demo = new ProgramSearchDemo(catalog);

        demo.search("computer science", Set.of());

        catalog.replace(new SqliteCatalogLoader().load(Paths.get("data", "dgx.db")));

        demo.search("computer science", Set.of());
        demo.search("asmaj1689", Set.of());
        demo.search("ASMIN 1689", Set.of());
        demo.search("stat", Set.of("Major"));
        demo.search("", Set.of("Certificate"));
        demo.search("", Set.of());
        demo.search("quantum basket weaving", Set.of());
    }
}
