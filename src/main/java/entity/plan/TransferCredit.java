package entity.plan;

import java.util.Objects;

/**
 * Credit from another institution or from AP or IB exams. It always counts as taken and has no term or grade. The
 * constructor doesn't validate the code, because generic codes such as {@code MAT1**} exist.
 */
public final class TransferCredit {
    private final String code;
    private final double weight;

    /** @throws IllegalArgumentException if {@code weight}, in credits, isn't finite and greater than 0 */
    public TransferCredit(String code, double weight) {
        this.code = Objects.requireNonNull(code, "code");
        if (!Double.isFinite(weight) || weight <= 0) {
            throw new IllegalArgumentException("weight must be > 0, was " + weight);
        }
        this.weight = weight;
    }

    public String getCode() {
        return code;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransferCredit)) {
            return false;
        }
        TransferCredit that = (TransferCredit) o;
        return code.equals(that.code) && Double.compare(weight, that.weight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, weight);
    }

    @Override
    public String toString() {
        return "TransferCredit{" + code + ", " + weight + "}";
    }
}
