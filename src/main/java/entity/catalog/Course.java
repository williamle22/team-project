package entity.catalog;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Course {
    private final String code;
    private final String designator;
    private final String number;
    private final int level;
    private final double weight;
    private final Campus campus;
    private final String title;
    private final String description;
    private final Requisite prerequisites;
    private final Requisite corequisites;
    private final Exclusions exclusions;
    private final List<BreadthCategory> breadth;
    private final Sessions sessions;

    public Course(String code, String designator, String number, int level, double weight,
                  Campus campus, String title, String description,
                  Requisite prerequisites, Requisite corequisites, Exclusions exclusions,
                  List<BreadthCategory> breadth, Sessions sessions) {
        this.code = Objects.requireNonNull(code, "code");
        this.designator = Objects.requireNonNull(designator, "designator");
        this.number = Objects.requireNonNull(number, "number");
        if (level < 100 || level > 400 || level % 100 != 0) {
            throw new IllegalArgumentException("level must be one of [100, 200, 300, 400], was " + level);
        }
        this.level = level;
        if (weight != 0.5 && weight != 1.0) {
            throw new IllegalArgumentException("weight must be 0.5 or 1.0, was " + weight);
        }
        this.weight = weight;
        this.campus = Objects.requireNonNull(campus, "campus");
        this.title = Objects.requireNonNull(title, "title");
        this.description = Objects.requireNonNull(description, "description");
        this.prerequisites = prerequisites;
        this.corequisites = corequisites;
        this.exclusions = exclusions;
        this.breadth = List.copyOf(Objects.requireNonNull(breadth, "breadth"));
        this.sessions = sessions;
    }

    public String getCode() {
        return code;
    }

    public String getDesignator() {
        return designator;
    }

    public String getNumber() {
        return number;
    }

    public int getLevel() {
        return level;
    }

    public double getWeight() {
        return weight;
    }

    public Campus getCampus() {
        return campus;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Optional<Requisite> getPrerequisites() {
        return Optional.ofNullable(prerequisites);
    }

    public Optional<Requisite> getCorequisites() {
        return Optional.ofNullable(corequisites);
    }

    public Optional<Exclusions> getExclusions() {
        return Optional.ofNullable(exclusions);
    }

    public List<BreadthCategory> getBreadth() {
        return breadth;
    }

    public Optional<Sessions> getSessions() {
        return Optional.ofNullable(sessions);
    }

    public boolean isOfferedThisYear() {
        return sessions != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Course)) {
            return false;
        }
        return code.equals(((Course) o).code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return "Course{" + code + ", " + title + "}";
    }
}
