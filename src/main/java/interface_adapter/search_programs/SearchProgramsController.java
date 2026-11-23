package interface_adapter.search_programs;

import entity.catalog.ProgramType;
import use_case.search_programs.SearchProgramsInputBoundary;
import use_case.search_programs.SearchProgramsInputData;

import java.util.Set;
import java.util.stream.Collectors;

/** Starts Use Case 2's program search. The search runs on the calling thread, because it's fast. */
public final class SearchProgramsController {
    private final SearchProgramsInputBoundary interactor;

    public SearchProgramsController(SearchProgramsInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Searches with the picker's current values. {@code types} holds program type display names, such as
     * {@code "Major"}, and an empty set means any type.
     *
     * @throws IllegalArgumentException if a type name is unknown
     */
    public void execute(String query, Set<String> types) {
        interactor.execute(new SearchProgramsInputData(query,
                types.stream().map(ProgramType::fromString).collect(Collectors.toSet())));
    }
}
