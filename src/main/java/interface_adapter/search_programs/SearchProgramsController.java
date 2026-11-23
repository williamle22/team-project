package interface_adapter.search_programs;

import entity.catalog.ProgramType;
import use_case.search_programs.SearchProgramsInputBoundary;
import use_case.search_programs.SearchProgramsInputData;

import java.util.Set;
import java.util.stream.Collectors;

public final class SearchProgramsController {
    private final SearchProgramsInputBoundary interactor;

    public SearchProgramsController(SearchProgramsInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String query, Set<String> types) {
        interactor.execute(new SearchProgramsInputData(query,
                types.stream().map(ProgramType::fromString).collect(Collectors.toSet())));
    }
}
