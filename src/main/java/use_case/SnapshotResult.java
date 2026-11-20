package use_case;

import entity.evaluation.AcademicSnapshot;

import java.util.List;
import java.util.Objects;

public final class SnapshotResult {
    private final AcademicSnapshot snapshot;
    private final List<String> warnings;

    public SnapshotResult(AcademicSnapshot snapshot, List<String> warnings) {
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
        this.warnings = List.copyOf(Objects.requireNonNull(warnings, "warnings"));
    }

    public AcademicSnapshot getSnapshot() {
        return snapshot;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SnapshotResult)) {
            return false;
        }
        SnapshotResult that = (SnapshotResult) o;
        return snapshot.equals(that.snapshot) && warnings.equals(that.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshot, warnings);
    }

    @Override
    public String toString() {
        return "SnapshotResult{" + snapshot + ", warnings=" + warnings + "}";
    }
}
