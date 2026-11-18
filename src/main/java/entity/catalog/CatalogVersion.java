package entity.catalog;

import java.time.Instant;
import java.util.Objects;

/** The catalog version ({@code version} table): its school year and when the backend last updated the catalog. */
public final class CatalogVersion {
    private final String schoolYear;
    private final Instant lastUpdated;

    public CatalogVersion(String schoolYear, Instant lastUpdated) {
        this.schoolYear = Objects.requireNonNull(schoolYear, "schoolYear");
        this.lastUpdated = Objects.requireNonNull(lastUpdated, "lastUpdated");
    }

    /** Returns the school year, such as {@code "2026-2027"}. */
    public String getSchoolYear() {
        return schoolYear;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CatalogVersion)) {
            return false;
        }
        CatalogVersion that = (CatalogVersion) o;
        return schoolYear.equals(that.schoolYear) && lastUpdated.equals(that.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schoolYear, lastUpdated);
    }

    @Override
    public String toString() {
        return "CatalogVersion{" + schoolYear + ", " + lastUpdated + "}";
    }
}
