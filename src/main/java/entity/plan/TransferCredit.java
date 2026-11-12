package entity.plan;

import java.util.Objects;

public final class TransferCredit {
    private final String code;
    private final double weight;

    public TransferCredit(String code, double weight) {
        this.code = Objects.requireNonNull(code, "code");
        if (weight <= 0) {
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
        return code.equals(that.code) && weight == that.weight;
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
