package entity.evaluation;

import java.util.Objects;

public final class ExclusionReason {
    private final String text;
    private final ExclusionResult result;

    public ExclusionReason(String text, ExclusionResult result) {
        this.text = Objects.requireNonNull(text, "text");
        this.result = Objects.requireNonNull(result, "result");
    }

    public String getText() {
        return text;
    }

    public ExclusionResult getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExclusionReason)) {
            return false;
        }
        ExclusionReason that = (ExclusionReason) o;
        return text.equals(that.text) && result == that.result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, result);
    }

    @Override
    public String toString() {
        return "ExclusionReason{" + result + " \"" + text + "\"}";
    }
}
