package entity.requirement;

import java.util.List;
import java.util.Objects;

public final class ProgramReq extends Requirement {
    private final List<String> programs;
    private final String text;

    public ProgramReq(List<String> programs, String text, boolean concurrent) {
        super(concurrent);
        this.programs = List.copyOf(Objects.requireNonNull(programs, "programs"));
        if (programs.isEmpty()) {
            throw new IllegalArgumentException("programs is empty");
        }
        this.text = Objects.requireNonNull(text, "text");
    }

    public List<String> getPrograms() {
        return programs;
    }

    public String getText() {
        return text;
    }

    @Override
    public <R> R accept(RequirementVisitor<R> visitor) {
        return visitor.visitProgram(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        ProgramReq that = (ProgramReq) o;
        return programs.equals(that.programs) && text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), programs, text);
    }
}
