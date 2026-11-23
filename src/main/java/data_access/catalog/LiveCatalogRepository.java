package data_access.catalog;

import entity.catalog.CatalogVersion;
import entity.catalog.Course;
import entity.catalog.Program;
import entity.catalog.ProgramType;
import use_case.CatalogReader;
import use_case.search_courses.SearchCoursesDataAccessInterface;
import use_case.search_courses.SearchFilters;
import use_case.search_programs.SearchProgramsDataAccessInterface;
import use_case.sync_catalog.LiveCatalog;

import java.util.*;

/**
 * The shared {@link CatalogReader} that every use case reads. It forwards each call to the current catalog, which
 * starts empty. UC1 replaces it when it loads a catalog. You can read it from any thread. It also searches the
 * current catalog's courses and programs for UC2, using indexes that it rebuilds each time UC1 replaces the catalog.
 */
public final class LiveCatalogRepository implements CatalogReader, LiveCatalog,
        SearchCoursesDataAccessInterface, SearchProgramsDataAccessInterface {
    private volatile CatalogReader current = InMemoryCatalogRepository.empty();
    private volatile SearchIndex<Course> courseIndex = SearchIndex.empty();
    private volatile SearchIndex<Program> programIndex = SearchIndex.empty();

    @Override
    public void replace(CatalogReader catalog) {
        Objects.requireNonNull(catalog, "catalog");
        courseIndex = new SearchIndex<>(catalog.allCourses(), Course::getCode, Course::getTitle);
        programIndex = new SearchIndex<>(catalog.allPrograms(), Program::getCode, Program::getTitle);
        current = catalog;
    }

    @Override
    public Optional<Course> findCourse(String code) {
        return current.findCourse(code);
    }

    @Override
    public Optional<Program> findProgram(String code) {
        return current.findProgram(code);
    }

    @Override
    public Collection<Course> allCourses() {
        return current.allCourses();
    }

    @Override
    public Collection<Program> allPrograms() {
        return current.allPrograms();
    }

    @Override
    public Optional<CatalogVersion> version() {
        return current.version();
    }

    @Override
    public boolean isLoaded() {
        return current.isLoaded();
    }

    @Override
    public List<Course> search(String query, SearchFilters filters) {
        return courseIndex.search(query, filters::accepts);
    }

    @Override
    public List<Program> searchPrograms(String query, Set<ProgramType> types) {
        return programIndex.search(query, p -> types.isEmpty() || types.contains(p.getType()));
    }
}
