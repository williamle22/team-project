package use_case;

import entity.catalog.CatalogVersion;
import entity.catalog.Course;
import entity.catalog.Program;

import java.util.Collection;
import java.util.Optional;

public interface CatalogReader {
    Optional<Course> findCourse(String code);

    Optional<Program> findProgram(String code);

    Collection<Course> allCourses();

    Collection<Program> allPrograms();

    Optional<CatalogVersion> version();

    boolean isLoaded();
}
