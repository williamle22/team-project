package data_access.catalog;

import entity.catalog.*;
import org.sqlite.SQLiteConfig;
import use_case.CatalogLoadException;
import use_case.CatalogOutdatedException;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Loads a catalog from the SQLite file that dgx-backend serves. */
public final class SqliteCatalogLoader {
    /** The {@code PRAGMA user_version} that this app reads. */
    public static final int SUPPORTED_SCHEMA_VERSION = 8;

    private final CatalogJsonMapper mapper = new CatalogJsonMapper();

    /**
     * Reads every course and program in {@code dbFile} into memory, and prints the load time to standard error.
     *
     * @throws CatalogOutdatedException if the file's schema version is lower than {@link #SUPPORTED_SCHEMA_VERSION}
     * @throws CatalogLoadException if the file is missing or unreadable, its schema version is higher, it has no
     *     version row, or a row is malformed
     */
    public InMemoryCatalogRepository load(Path dbFile) {
        long start = System.nanoTime();
        SQLiteConfig config = new SQLiteConfig();
        config.setReadOnly(true);

        try (Connection conn = config.createConnection("jdbc:sqlite:" + dbFile.toAbsolutePath())) {
            checkSchemaVersion(conn);
            CatalogVersion version = loadVersion(conn);
            List<Course> courses = loadCourses(conn);
            List<Program> programs = loadPrograms(conn);

            long ms = (System.nanoTime() - start) / 1_000_000;
            System.err.println("loaded " + courses.size() + " courses and " + programs.size() + " programs in " + ms + " ms");

            return new InMemoryCatalogRepository(courses, programs, version);
        } catch (SQLException e) {
            throw new CatalogLoadException("could not read catalog at " + dbFile, e);
        }
    }

    private void checkSchemaVersion(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("PRAGMA user_version")) {
            int v = rs.getInt(1);

            if (v > SUPPORTED_SCHEMA_VERSION) {
                throw new CatalogLoadException("catalog schema " + v + " is newer than this app supports; update the app");
            }
            if (v < SUPPORTED_SCHEMA_VERSION) {
                throw new CatalogOutdatedException("catalog schema " + v + " is older than " + SUPPORTED_SCHEMA_VERSION + "; download it again");
            }
        }
    }

    private CatalogVersion loadVersion(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT school_year, last_updated FROM version ORDER BY school_year DESC LIMIT 1")) {
            if (!rs.next()) {
                throw new CatalogLoadException("catalog has no version row");
            }
            return new CatalogVersion(rs.getString("school_year"), Instant.parse(rs.getString("last_updated")));
        }
    }

    private List<Course> loadCourses(Connection conn) throws SQLException {
        List<Course> courses = new ArrayList<>();

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT code, designator, number, level, weight, campus, title, description, prerequisites, corequisites, exclusions, breadth, sessions FROM courses")) {
            while (rs.next()) {
                String code = rs.getString("code");
                try {
                    courses.add(new Course(
                            code, rs.getString("designator"), rs.getString("number"), rs.getInt("level"), rs.getDouble("weight"),
                            Campus.fromString(rs.getString("campus")), rs.getString("title"), rs.getString("description"),
                            mapper.parseRequisite(rs.getString("prerequisites")), mapper.parseRequisite(rs.getString("corequisites")),
                            mapper.parseExclusions(rs.getString("exclusions")), mapper.parseBreadth(rs.getString("breadth")),
                            mapper.parseSessions(rs.getString("sessions"))
                    ));
                } catch (RuntimeException e) {
                    throw new CatalogLoadException("bad row for course " + code, e);
                }
            }
        }

        return courses;
    }

    private List<Program> loadPrograms(Connection conn) throws SQLException {
        List<Program> programs = new ArrayList<>();

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT code, type, number, title, enrolment, completion, areas FROM programs")) {
            while (rs.next()) {
                String code = rs.getString("code");
                try {
                    programs.add(new Program(
                            code, ProgramType.fromString(rs.getString("type")), rs.getString("number"), rs.getString("title"),
                            mapper.parseEnrolment(rs.getString("enrolment")), mapper.parseCompletion(rs.getString("completion")),
                            mapper.parseAreas(rs.getString("areas"))
                    ));
                } catch (RuntimeException e) {
                    throw new CatalogLoadException("bad row for program " + code, e);
                }
            }
        }

        return programs;
    }
}
