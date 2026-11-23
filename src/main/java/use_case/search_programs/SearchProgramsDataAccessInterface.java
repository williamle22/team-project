package use_case.search_programs;

import entity.catalog.Program;
import entity.catalog.ProgramType;

import java.util.List;
import java.util.Set;

public interface SearchProgramsDataAccessInterface {
    boolean isLoaded();
    List<Program> searchPrograms(String query, Set<ProgramType> types);
}
