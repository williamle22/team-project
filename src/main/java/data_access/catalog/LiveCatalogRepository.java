package data_access.catalog;

import entity.catalog.CatalogVersion;
import entity.catalog.Course;
import entity.catalog.Program;
import use_case.CatalogReader;
import use_case.sync_catalog.LiveCatalog;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * The shared {@link CatalogReader} that every use case reads. It forwards each call to the current catalog, which
 * starts empty. UC1 replaces it when it loads a catalog. You can read it from any thread.
 */
public final class LiveCatalogRepository implements CatalogReader, LiveCatalog {
    private volatile CatalogReader current = InMemoryCatalogRepository.empty();

    @Override
    public void replace(CatalogReader catalog) {
        current = Objects.requireNonNull(catalog, "catalog");
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
}
