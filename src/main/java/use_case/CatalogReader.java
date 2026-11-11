package use_case;

import entity.catalog.CatalogVersion;
import entity.catalog.Course;
import entity.catalog.Program;

import java.util.Collection;
import java.util.Optional;

/** Read-only access to the course catalog. Use cases depend on this interface, not on an implementation. */
public interface CatalogReader {
    /** Finds a course by code, ignoring case and spaces: {@code "csc 148h1"} finds {@code CSC148H1}. */
    Optional<Course> findCourse(String code);

    /** Finds a program by code, ignoring case and spaces. */
    Optional<Program> findProgram(String code);

    /** Returns every course, sorted by code. */
    Collection<Course> allCourses();

    /** Returns every program, sorted by code. */
    Collection<Program> allPrograms();

    /** Returns the catalog's version, or an empty {@code Optional} until a catalog loads. */
    Optional<CatalogVersion> version();

    boolean isLoaded();
}
