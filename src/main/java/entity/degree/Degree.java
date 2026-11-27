package entity.degree;

import entity.catalog.Completion;

import java.util.List;
import java.util.Objects;

/**
 * A degree's completion rules, in the same row format as a program's {@code Completion}, and the program combinations
 * it accepts. If {@code eligiblePrograms} isn't empty, only those programs count toward a combination, as with BCom's
 * three Specialists. Degrees with the same {@code type} are equal.
 */
public final class Degree {
    private final DegreeType type;
    private final Completion completion;
    private final ProgramCombinationRule programCombinations;
    private final List<String> eligiblePrograms;

    public Degree(DegreeType type, Completion completion, ProgramCombinationRule programCombinations,
                  List<String> eligiblePrograms) {
        this.type = Objects.requireNonNull(type, "type");
        this.completion = Objects.requireNonNull(completion, "completion");
        this.programCombinations = Objects.requireNonNull(programCombinations, "programCombinations");
        this.eligiblePrograms = List.copyOf(Objects.requireNonNull(eligiblePrograms, "eligiblePrograms"));
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

    public List<String> getEligiblePrograms() {
        return eligiblePrograms;
    }

    public boolean countsTowardCombination(String programCode) {
        return eligiblePrograms.isEmpty() || eligiblePrograms.contains(programCode);
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
