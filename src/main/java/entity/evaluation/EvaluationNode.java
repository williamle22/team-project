package entity.evaluation;

import entity.requirement.Requirement;

import java.util.*;

public final class EvaluationNode {
    private final Requirement requirement;
    private final Status status;
    private final String reason;
    private final List<EvaluationNode> children;
    private final List<String> counted;

    public EvaluationNode(Requirement requirement, Status status, String reason,
                          List<EvaluationNode> children, List<String> counted) {
        this.requirement = requirement;
        this.status = Objects.requireNonNull(status, "status");
        this.reason = reason;
        this.children = List.copyOf(Objects.requireNonNull(children, "children"));
        this.counted = List.copyOf(Objects.requireNonNull(counted, "counted"));
    }

    public static EvaluationNode leaf(Requirement r, Status s, String reason) {
        return new EvaluationNode(r, s, reason, List.of(), List.of());
    }

    public static EvaluationNode leaf(Requirement r, Status s, String reason, List<String> counted) {
        return new EvaluationNode(r, s, reason, List.of(), counted);
    }

    public static EvaluationNode none(String reason) {
        return new EvaluationNode(null, Status.MET, reason, List.of(), List.of());
    }

    public Optional<Requirement> getRequirement() {
        return Optional.ofNullable(requirement);
    }

    public Status getStatus() {
        return status;
    }

    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }

    public List<EvaluationNode> getChildren() {
        return children;
    }

    public List<String> getCounted() {
        return counted;
    }

    public Set<String> allCounted() {
        Set<String> all = new LinkedHashSet<>(counted);

        for (EvaluationNode c : children) {
            all.addAll(c.allCounted());
        }

        return all;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EvaluationNode)) {
            return false;
        }
        EvaluationNode that = (EvaluationNode) o;
        return Objects.equals(requirement, that.requirement)
                && status == that.status
                && Objects.equals(reason, that.reason)
                && children.equals(that.children)
                && counted.equals(that.counted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirement, status, reason, children, counted);
    }

    @Override
    public String toString() {
        return "EvaluationNode{" + status + ", reason=" + reason + ", requirement=" + requirement + ", counted="
                + counted + ", children=" + children + "}";
    }
}
