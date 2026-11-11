package entity.requirement;

/**
 * A node of a requirement tree. Each subclass is one node type of the backend's requirement JSON.
 *
 * <p>When {@code concurrent} is true, a course the student is taking counts as taken within this subtree.
 * Subclass {@code equals} methods call {@code super.equals(o)} first, which checks the class and
 * {@code concurrent}.
 */
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