package entity.grades;

import java.util.Objects;
import java.util.Optional;

/**
 * One graded part of a course, such as a midterm. The weight is a percent of the final grade. The score is a percent,
 * is empty until graded, and can exceed 100 for bonus marks.
 */
public final class Assessment {
    private final String name;
    private final double weight;
    private final Double score;

    /**
     * @throws IllegalArgumentException if {@code weight} isn't positive and finite, or {@code score} is negative or
     *     not finite
     */
    public Assessment(String name, double weight, Double score) {
        this.name = Objects.requireNonNull(name, "name");
        if (!Double.isFinite(weight) || weight <= 0) {
            throw new IllegalArgumentException("weight must be > 0, was " + weight);
        }
        if (score != null && (!Double.isFinite(score) || score < 0)) {
            throw new IllegalArgumentException("score must be >= 0, was " + score);
        }
        this.weight = weight;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public Optional<Double> getScore() {
        return Optional.ofNullable(score);
    }

    public boolean isGraded() {
        return score != null;
    }

    public Assessment withScore(Double score) {
        return new Assessment(name, weight, score);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Assessment)) {
            return false;
        }
        Assessment that = (Assessment) o;
        return name.equals(that.name) && Double.compare(weight, that.weight) == 0 && Objects.equals(score, that.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weight, score);
    }

    @Override
    public String toString() {
        return "Assessment{" + name + ", " + weight + "%" + (score == null ? "" : ", " + score) + "}";
    }
}
