package data_access.catalog;

import entity.catalog.CatalogVersion;
import entity.catalog.Course;
import entity.catalog.Program;
import use_case.CatalogReader;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public final class InMemoryCatalogRepository implements CatalogReader {
    private final Map<String, Course> courses = new TreeMap<>();
    private final Map<String, Program> programs = new TreeMap<>();
    private final CatalogVersion version;

    public InMemoryCatalogRepository(Collection<Course> courses, Collection<Program> programs,
                                     CatalogVersion version) {
        for (Course course : courses) {
            if (this.courses.put(course.getCode(), course) != null) {
                throw new IllegalArgumentException("duplicate course code " + course.getCode());
            }
        }
        for (Program program : programs) {
            if (this.programs.put(program.getCode(), program) != null) {
                throw new IllegalArgumentException("duplicate program code " + program.getCode());
            }
        }
        this.version = Objects.requireNonNull(version, "version");
    }

    private InMemoryCatalogRepository() {
        this.version = null;
    }

    public static InMemoryCatalogRepository empty() {
        return new InMemoryCatalogRepository();
    }

    @Override
    public Optional<Course> findCourse(String code) {
        return Optional.ofNullable(courses.get(normalize(code)));
    }

    @Override
    public Optional<Program> findProgram(String code) {
        return Optional.ofNullable(programs.get(normalize(code)));
    }

    @Override
    public Collection<Course> allCourses() {
        return Collections.unmodifiableCollection(courses.values());
    }

    @Override
    public Collection<Program> allPrograms() {
        return Collections.unmodifiableCollection(programs.values());
    }

    @Override
    public Optional<CatalogVersion> version() {
        return Optional.ofNullable(version);
    }

    @Override
    public boolean isLoaded() {
        return version != null;
    }

    private static String normalize(String code) {
        return code.toUpperCase(Locale.ROOT).replace(" ", "");
    }
}
