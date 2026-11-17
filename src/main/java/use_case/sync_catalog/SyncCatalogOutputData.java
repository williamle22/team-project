package use_case.sync_catalog;

import java.time.Instant;
import java.util.Objects;

public final class SyncCatalogOutputData {
    public enum Outcome { UPDATED, UP_TO_DATE, OFFLINE_USING_CACHE, UPDATE_FAILED_USING_CACHE }

    private final Outcome outcome;
    private final String schoolYear;
    private final Instant lastUpdated;
    private final int courseCount;
    private final int programCount;

    public SyncCatalogOutputData(Outcome outcome, String schoolYear, Instant lastUpdated, int courseCount, int programCount) {
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
