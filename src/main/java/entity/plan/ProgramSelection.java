package entity.plan;

import java.util.Objects;

/** A program on a plan, with its {@link ProgramStatus}. It stores the program code, not the {@code Program}. */
public final class ProgramSelection {
    private final String code;
    private final ProgramStatus status;

    public ProgramSelection(String code, ProgramStatus status) {
        this.code = Objects.requireNonNull(code, "code");
        this.status = Objects.requireNonNull(status, "status");
    }

    public String getCode() {
        return code;
    }

    public ProgramStatus getStatus() {
        return status;
    }

    public ProgramSelection withStatus(ProgramStatus status) {
        return new ProgramSelection(code, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProgramSelection)) {
            return false;
        }
        ProgramSelection that = (ProgramSelection) o;
        return code.equals(that.code) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, status);
    }

    @Override
    public String toString() {
        return "ProgramSelection{" + code + ", " + status + "}";
    }
}
