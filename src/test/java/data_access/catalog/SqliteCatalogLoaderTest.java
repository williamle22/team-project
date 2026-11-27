package data_access.catalog;

import entity.catalog.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testutil.JsonFixtures;
import use_case.CatalogLoadException;
import use_case.CatalogOutdatedException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SqliteCatalogLoaderTest {

    private static final int SUPPORTED = SqliteCatalogLoader.SUPPORTED_SCHEMA_VERSION;
    private static final int OLD_SCHEMA = SUPPORTED - 1;
    private static final int NEW_SCHEMA = SUPPORTED + 1;

    private static final String SCHEMA = "CREATE TABLE version (school_year TEXT PRIMARY KEY NOT NULL, "
            + "last_updated TEXT NOT NULL) WITHOUT ROWID;"
            + "CREATE TABLE courses (code TEXT PRIMARY KEY NOT NULL, designator TEXT NOT NULL, number TEXT NOT NULL, "
            + "level INTEGER NOT NULL, weight FLOAT NOT NULL, campus TEXT NOT NULL, title TEXT NOT NULL, "
            + "description TEXT NOT NULL, prerequisites TEXT, corequisites TEXT, exclusions TEXT, breadth TEXT, "
            + "sessions TEXT) WITHOUT ROWID;"
            + "CREATE TABLE programs (code TEXT PRIMARY KEY NOT NULL, type TEXT NOT NULL, number TEXT NOT NULL, "
            + "title TEXT NOT NULL, enrolment TEXT, completion TEXT, areas TEXT) WITHOUT ROWID;";

    @TempDir
    Path tmp;

    private final SqliteCatalogLoader loader = new SqliteCatalogLoader();

    private Path tinyDatabase(int schemaVersion) throws SQLException {
        Path file = tmp.resolve("catalog.db");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file);
             Statement st = conn.createStatement()) {
            for (String sql : SCHEMA.split(";")) {
                st.execute(sql);
            }
            st.execute("PRAGMA user_version = " + schemaVersion);
            st.execute("INSERT INTO version VALUES ('2025-2026', '2025-09-01T00:00:00Z')");
            st.execute("INSERT INTO version VALUES ('2026-2027', '2026-11-05T18:34:42Z')");
        }
        return file;
    }

    private void execute(Path file, String sql) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file);
             Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    private void insertCourse(Path file, JSONObject row) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file);
             PreparedStatement ps = conn.prepareStatement("INSERT INTO courses VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            String[] cols = {"code", "designator", "number", "level", "weight", "campus", "title", "description",
                    "prerequisites", "corequisites", "exclusions", "breadth", "sessions"};
            for (int i = 0; i < cols.length; i++) {
                ps.setObject(i + 1, column(row, cols[i]));
            }
            ps.executeUpdate();
        }
    }

    private void insertProgram(Path file, JSONObject row) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file);
             PreparedStatement ps = conn.prepareStatement("INSERT INTO programs VALUES (?,?,?,?,?,?,?)")) {
            String[] cols = {"code", "type", "number", "title", "enrolment", "completion", "areas"};
            for (int i = 0; i < cols.length; i++) {
                ps.setObject(i + 1, column(row, cols[i]));
            }
            ps.executeUpdate();
        }
    }

    private static Object column(JSONObject row, String key) {
        if (row.isNull(key)) {
            return null;
        }
        Object v = row.get(key);
        return v instanceof JSONObject || v instanceof JSONArray ? v.toString() : v;
    }

    private static JSONObject fixture(String name) {
        return new JSONObject(JsonFixtures.read(name));
    }

    @Test
    void parsesEveryRowOfTheRealCatalog() {
        Path real = Paths.get("data", "dgx.db");
        assumeTrue(Files.exists(real), "no data/dgx.db");

        InMemoryCatalogRepository repo = loader.load(real);
        assertTrue(repo.allCourses().size() > 1000);
        assertTrue(repo.allPrograms().size() > 100);
        assertTrue(repo.findCourse("CSC148H1").isPresent());
        assertTrue(repo.findProgram("ASMIN1001").isPresent());
        assertTrue(repo.allCourses().stream().anyMatch(c -> c.getSessions().isPresent()));
    }

    @Test
    void loadsTinyDatabase() throws SQLException {
        Path file = tinyDatabase(SUPPORTED);
        insertCourse(file, fixture("PHY152H1.json"));
        insertProgram(file, fixture("ASMIN0667.json"));

        InMemoryCatalogRepository repo = loader.load(file);
        assertEquals(new CatalogVersion("2026-2027", Instant.parse("2026-11-05T18:34:42Z")), repo.version().orElseThrow());

        Course phy = repo.findCourse("PHY152H1").orElseThrow();
        assertEquals("152", phy.getNumber());
        assertEquals(100, phy.getLevel());
        assertEquals(0.5, phy.getWeight());
        assertEquals(Campus.UTSG, phy.getCampus());
        assertTrue(phy.getPrerequisites().isPresent());
        assertTrue(phy.getSessions().orElseThrow().offeredInWinter());

        Program hps = repo.findProgram("ASMIN0667").orElseThrow();
        assertEquals(ProgramType.MINOR, hps.getType());
        assertEquals(3, hps.getCompletion().orElseThrow().getRequirements().size());
        assertEquals(1, repo.allCourses().size());
        assertEquals(1, repo.allPrograms().size());
    }

    @Test
    void nullJsonColumnsLoad() throws SQLException {
        Path file = tinyDatabase(SUPPORTED);
        execute(file, "INSERT INTO courses VALUES ('CSC199H1', 'CSC', '199', 100, 0.5, 'utsg', 'First-Year Seminar', "
                + "'A seminar.', NULL, NULL, NULL, NULL, NULL)");
        execute(file, "INSERT INTO programs VALUES ('ASMIN9999', 'Minor', '9999', 'Test Minor', NULL, NULL, NULL)");

        InMemoryCatalogRepository repo = loader.load(file);
        Course c = repo.findCourse("CSC199H1").orElseThrow();
        assertTrue(c.getPrerequisites().isEmpty());
        assertTrue(c.getSessions().isEmpty());
        assertTrue(c.getBreadth().isEmpty());
        Program p = repo.findProgram("ASMIN9999").orElseThrow();
        assertTrue(p.getEnrolment().isEmpty());
        assertTrue(p.getAreas().isEmpty());
    }

    @Test
    void rejectsNewerSchema() throws SQLException {
        Path file = tinyDatabase(NEW_SCHEMA);
        CatalogLoadException e = assertThrows(CatalogLoadException.class, () -> loader.load(file));
        assertFalse(e instanceof CatalogOutdatedException);
        assertTrue(e.getMessage().contains("newer"));
    }

    @Test
    void rejectsOlderSchemaAsOutdated() throws SQLException {
        Path file = tinyDatabase(OLD_SCHEMA);
        assertThrows(CatalogOutdatedException.class, () -> loader.load(file));
    }

    @Test
    void missingVersionRowThrows() throws SQLException {
        Path file = tinyDatabase(SUPPORTED);
        execute(file, "DELETE FROM version");
        CatalogLoadException e = assertThrows(CatalogLoadException.class, () -> loader.load(file));
        assertEquals("catalog has no version row", e.getMessage());
    }

    @Test
    void missingFileThrows() {
        Path file = tmp.resolve("missing.db");
        CatalogLoadException e = assertThrows(CatalogLoadException.class, () -> loader.load(file));
        assertTrue(e.getMessage().startsWith("could not read catalog at "));
        assertInstanceOf(SQLException.class, e.getCause());
        assertFalse(Files.exists(file));
    }

    @Test
    void badCourseRowNamesTheCourse() throws SQLException {
        Path file = tinyDatabase(SUPPORTED);
        execute(file, "INSERT INTO courses VALUES ('CSC199H1', 'CSC', '199', 100, 0.5, 'mars', 'Seminar', "
                + "'A seminar.', NULL, NULL, NULL, NULL, NULL)");
        CatalogLoadException e = assertThrows(CatalogLoadException.class, () -> loader.load(file));
        assertEquals("bad row for course CSC199H1", e.getMessage());
        assertInstanceOf(IllegalArgumentException.class, e.getCause());
    }

    @Test
    void badProgramJsonNamesTheProgram() throws SQLException {
        Path file = tinyDatabase(SUPPORTED);
        execute(file, "INSERT INTO programs VALUES ('ASMIN9999', 'Minor', '9999', 'Test Minor', NULL, "
                + "'{\"raw\":\"no rows\"}', NULL)");
        CatalogLoadException e = assertThrows(CatalogLoadException.class, () -> loader.load(file));
        assertEquals("bad row for program ASMIN9999", e.getMessage());
        assertInstanceOf(CatalogFormatException.class, e.getCause());
    }
}
