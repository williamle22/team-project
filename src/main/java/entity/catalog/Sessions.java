package entity.catalog;

import java.util.List;
import java.util.Objects;

public final class Sessions {
    private final List<Section> fallWinter;
    private final List<Section> summer;

    public Sessions(List<Section> fallWinter, List<Section> summer) {
        this.fallWinter = List.copyOf(Objects.requireNonNull(fallWinter, "fallWinter"));
        this.summer = List.copyOf(Objects.requireNonNull(summer, "summer"));
    }

    public List<Section> getFallWinter() {
        return fallWinter;
    }

    public List<Section> getSummer() {
        return summer;
    }

    public List<Section> sections(SessionPeriod period) {
        return period == SessionPeriod.SUMMER ? summer : fallWinter;
    }

    public boolean offers(SessionPeriod period, Section section) {
        return sections(period).contains(section);
    }

    public boolean offeredInFall() {
        return this.fallWinter.contains(Section.F) || fallWinter.contains(Section.Y);
    }

    public boolean offeredInWinter() {
        return this.fallWinter.contains(Section.S) || fallWinter.contains(Section.Y);
    }

    public boolean offeredInSummer() {
        return !summer.isEmpty();
    }

    public boolean isFullYear() {
        return this.fallWinter.contains(Section.Y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sessions)) {
            return false;
        }
        Sessions that = (Sessions) o;
        return fallWinter.equals(that.fallWinter) && summer.equals(that.summer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fallWinter, summer);
    }

    @Override
    public String toString() {
        return "Sessions{fallWinter=" + fallWinter + ", summer=" + summer + "}";
    }
}