package use_case.search_programs;

import entity.catalog.Program;
import use_case.CodeRanking;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case 2: ranks the matching programs with {@link CodeRanking}. Unlike the course search, it keeps every match,
 * because the catalog has few enough programs to list them all.
 */
public final class SearchProgramsInteractor implements SearchProgramsInputBoundary {
    private final SearchProgramsDataAccessInterface catalog;
    private final SearchProgramsOutputBoundary presenter;

    public SearchProgramsInteractor(SearchProgramsDataAccessInterface catalog,
                                    SearchProgramsOutputBoundary presenter) {
        this.catalog = catalog;
        this.presenter = presenter;
    }

    @Override
    public void execute(SearchProgramsInputData input) {
        if (!catalog.isLoaded()) {
            presenter.prepareFailView("Program data is still loading.");
            return;
        }

        List<Program> matches = new ArrayList<>(catalog.searchPrograms(input.getQuery(), input.getTypes()));
        matches.sort(CodeRanking.by(input.getQuery(), Program::getCode));

        List<ProgramResultItem> items = matches.stream().map(SearchProgramsInteractor::toItem)
                .collect(Collectors.toList());

        presenter.prepareSuccessView(new SearchProgramsOutputData(items));
    }

    private static ProgramResultItem toItem(Program program) {
        return new ProgramResultItem(program.getCode(), program.getTitle(), program.getType());
    }
}
