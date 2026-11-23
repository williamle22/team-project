package use_case.search_programs;

import entity.catalog.Program;
import entity.catalog.ProgramType;

import java.util.List;
import java.util.Set;

/** The program search that Use Case 2 needs. */
public interface SearchProgramsDataAccessInterface {
    /** Returns whether a catalog has loaded. */
    boolean isLoaded();
    /**
     * Returns every program that matches {@code query} and has one of {@code types}, in no particular order. An empty
     * {@code types} accepts every type, and a blank query matches every program.
     */
    List<Program> searchPrograms(String query, Set<ProgramType> types);
}
