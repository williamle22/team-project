package entity.requirement;

public abstract class Requirement {
    private final boolean concurrent;

    protected Requirement(boolean concurrent) {
        this.concurrent = concurrent;
    }

    public boolean isConcurrent() {
        return concurrent;
    }

    public abstract <R> R accept(RequirementVisitor<R> visitor);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return concurrent == ((Requirement) o).concurrent;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(concurrent);
    }
}