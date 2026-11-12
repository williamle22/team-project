package use_case;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PlanSummary {
    private final UUID id;
    private final String name;
    private final Instant lastModified;

    public PlanSummary(UUID id, String name, Instant lastModified) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.lastModified = Objects.requireNonNull(lastModified, "lastModified");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlanSummary)) {
            return false;
        }
        PlanSummary that = (PlanSummary) o;
        return id.equals(that.id) && name.equals(that.name) && lastModified.equals(that.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, lastModified);
    }

    @Override
    public String toString() {
        return "PlanSummary{" + name + ", " + id + ", " + lastModified + "}";
    }
}
