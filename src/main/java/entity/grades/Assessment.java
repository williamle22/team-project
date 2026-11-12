package entity.grades;

import java.util.Objects;
import java.util.Optional;

public final class Assessment {
    private final String name;
    private final double weight;
    private final Double score;

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
