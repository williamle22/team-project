package use_case.search_programs;

import entity.catalog.Program;
import entity.catalog.ProgramType;

import java.util.List;
import java.util.Set;

class FakeProgramSearchCatalog implements SearchProgramsDataAccessInterface {
    boolean loaded = true;
    List<Program> matches = List.of();
    int searches;
    String lastQuery;
    Set<ProgramType> lastTypes;

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public List<Program> searchPrograms(String query, Set<ProgramType> types) {
        searches++;
        lastQuery = query;
        lastTypes = types;
        return List.copyOf(matches);
    }
}
