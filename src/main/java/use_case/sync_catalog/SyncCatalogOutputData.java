package use_case.sync_catalog;

import java.time.Instant;
import java.util.Objects;

/**
 * The catalog that Use Case 1 loaded, and how the app got it. {@code schoolYear} and {@code lastUpdated} come from the
 * catalog's {@code version} table.
 */
public final class SyncCatalogOutputData {
    public enum Outcome {
        /** The app downloaded and installed a new catalog. */
        UPDATED,
        /** The server's catalog matches the cache. */
        UP_TO_DATE,
        /** The app couldn't reach the server, so it uses the cache. */
        OFFLINE_USING_CACHE,
        /** The app couldn't load or install a download, so it uses the cache. */
        UPDATE_FAILED_USING_CACHE
    }

    private final Outcome outcome;
    private final String schoolYear;
    private final Instant lastUpdated;
    private final int courseCount;
    private final int programCount;

    public SyncCatalogOutputData(Outcome outcome, String schoolYear, Instant lastUpdated,
                                 int courseCount, int programCount) {
        this.outcome = Objects.requireNonNull(outcome, "outcome");
        this.schoolYear = Objects.requireNonNull(schoolYear, "schoolYear");
        this.lastUpdated = Objects.requireNonNull(lastUpdated, "lastUpdated");
        this.courseCount = courseCount;
        this.programCount = programCount;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public String getSchoolYear() {
        return schoolYear;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public int getCourseCount() {
        return courseCount;
    }

    public int getProgramCount() {
        return programCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SyncCatalogOutputData)) {
            return false;
        }
        SyncCatalogOutputData that = (SyncCatalogOutputData) o;
        return outcome == that.outcome && schoolYear.equals(that.schoolYear) && lastUpdated.equals(that.lastUpdated)
                && courseCount == that.courseCount && programCount == that.programCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(outcome, schoolYear, lastUpdated, courseCount, programCount);
    }

    @Override
    public String toString() {
        return "SyncCatalogOutputData{" + outcome + ", " + schoolYear + ", " + lastUpdated + ", "
                + courseCount + " courses, " + programCount + " programs}";
    }
}
