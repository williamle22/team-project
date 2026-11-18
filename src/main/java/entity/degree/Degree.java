package entity.degree;

import entity.catalog.Completion;

import java.util.Objects;

/**
 * A degree's completion rules, in the same row format as a program's {@code Completion}, and the program combinations
 * it accepts. Degrees with the same {@code type} are equal.
 */
public final class Degree {
    private final DegreeType type;
    private final Completion completion;
    private final ProgramCombinationRule programCombinations;

    public Degree(DegreeType type, Completion completion, ProgramCombinationRule programCombinations) {
        this.type = Objects.requireNonNull(type, "type");
        this.completion = Objects.requireNonNull(completion, "completion");
        this.programCombinations = Objects.requireNonNull(programCombinations, "programCombinations");
    }

    public DegreeType getType() {
        return type;
    }

    public String getTitle() {
        return type.getTitle();
    }

    public Completion getCompletion() {
        return completion;
    }

    public ProgramCombinationRule getProgramCombinations() {
        return programCombinations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Degree)) {
            return false;
        }
        return type == ((Degree) o).type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return "Degree{" + type + "}";
    }
}
