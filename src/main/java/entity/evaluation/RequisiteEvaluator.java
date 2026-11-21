package entity.evaluation;

import entity.requirement.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Evaluates a requisite against an {@link AcademicSnapshot}. Each node that isn't {@code MET} says what's missing, such
 * as "needs at least 60% in CSC148H1; you have 55%". Requirements that the app can't check, such as free text or a GPA
 * in a named subject, give {@code REVIEW}.
 */
public final class RequisiteEvaluator implements RequirementVisitor<EvaluationNode> {
    private final AcademicSnapshot snapshot;
    private final boolean concurrentAllowed;
    private final MembershipResolver lists;

    /**
     * {@code concurrentAllowed} sets whether concurrent courses count. A requirement marked concurrent always allows
     * them, for itself and every node under it.
     */
    public RequisiteEvaluator(AcademicSnapshot snapshot, boolean concurrentAllowed, MembershipResolver lists) {
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
        this.concurrentAllowed = concurrentAllowed;
        this.lists = Objects.requireNonNull(lists, "lists");
    }

    /** Evaluates {@code r} and returns a node tree with the same shape. */
    public EvaluationNode evaluate(Requirement r) {
        if (r.isConcurrent() && !concurrentAllowed) {
            return r.accept(new RequisiteEvaluator(snapshot, true, lists));
        }

        return r.accept(this);
    }

    @Override
    public EvaluationNode visitAnd(AndReq node) {
        List<EvaluationNode> children = evaluateAll(node.getItems());

        return new EvaluationNode(node, Status.allOf(statuses(children)), null, children, List.of());
    }

    @Override
    public EvaluationNode visitOr(OrReq node) {
        List<EvaluationNode> children = evaluateAll(node.getItems());

        return new EvaluationNode(node, Status.anyOf(statuses(children)), null, children, List.of());
    }

    @Override
    public EvaluationNode visitChoose(ChooseReq node) {
        List<EvaluationNode> children = evaluateAll(node.getItems());
        List<Status> s = statuses(children);
        int met = Collections.frequency(s, Status.MET);

        return new EvaluationNode(node, Status.atLeast(node.getCount(), s),
                met + " of " + node.getCount() + " met", children, List.of());
    }

    @Override
    public EvaluationNode visitNot(NotReq node) {
        EvaluationNode child = evaluate(node.getItem());
        Status s = child.getStatus().negate();

        if (s == Status.NOT_MET) {
            return new EvaluationNode(node, s,
                    "can't have " + new RequirementPrinter().print(node.getItem()), List.of(child), List.of());
        }

        return new EvaluationNode(node, s, null, List.of(child), List.of());
    }

    @Override
    public EvaluationNode visitCourse(CourseReq node) {
        String code = node.getCode();
        Optional<CourseRecord> r = find(code);

        if (r.isEmpty()) {
            return EvaluationNode.leaf(node, Status.NOT_MET, notTakenReason(code));
        }

        List<String> counted = List.of(code);

        if (node.getMinGrade().isEmpty()) {
            return EvaluationNode.leaf(node, Status.MET, null, counted);
        }

        int min = node.getMinGrade().get();
        Optional<Integer> grade = r.get().getGrade();

        if (grade.isEmpty()) {
            return EvaluationNode.leaf(node, Status.REVIEW,
                    "needs at least " + min + "% in " + code + "; no final grade yet", counted);
        }

        if (grade.get() < min) {
            return EvaluationNode.leaf(node, Status.NOT_MET,
                    "needs at least " + min + "% in " + code + "; you have " + grade.get() + "%", counted);
        }

        return EvaluationNode.leaf(node, Status.MET, null, counted);
    }

    @Override
    public EvaluationNode visitCredits(CreditsReq node) {
        double yes = 0;
        double maybe = 0;
        List<String> counted = new ArrayList<>();

        for (CourseRecord r : available()) {
            Match m = CourseMatcher.matches(node.getFilter(), r, lists);

            if (m == Match.NO) {
                continue;
            }

            double w = node.getWeight().orElse(r.getCourse().getWeight());
            maybe += w;

            if (m == Match.YES) {
                yes += w;
            }

            counted.add(r.getCode());
        }

        if (node.getMaxAmount().isPresent()) {
            yes = Math.min(yes, node.getMaxAmount().get());
            maybe = Math.min(maybe, node.getMaxAmount().get());
        }

        Status s;

        if (yes >= node.getAmount() - 1e-9) {
            s = Status.MET;
        } else if (maybe >= node.getAmount() - 1e-9) {
            s = Status.REVIEW;
        } else {
            s = Status.NOT_MET;
        }

        return EvaluationNode.leaf(node, s,
                s == Status.MET ? null : yes + " of " + node.getAmount() + " credits", counted);
    }

    @Override
    public EvaluationNode visitAverage(AverageReq node) {
        double weight = 0;
        double points = 0;
        List<String> counted = new ArrayList<>();

        if (!node.getItems().isEmpty() || node.isBest()) {
            return EvaluationNode.leaf(node, Status.REVIEW, "average over best credits; check manually", counted);
        }

        for (CourseRecord r : available()) {
            Match m = CourseMatcher.matches(node.getFilter(), r, lists);

            if (m == Match.NO) {
                continue;
            }

            if (m == Match.MAYBE) {
                return EvaluationNode.leaf(node, Status.REVIEW, "can't tell if " + r.getCode() + " counts", counted);
            }

            if (r.getGrade().isEmpty()) {
                return EvaluationNode.leaf(node, Status.REVIEW, "no final grade yet for " + r.getCode(), counted);
            }

            double w = r.getCourse().getWeight();
            weight += w;
            points += w * r.getGrade().get();
            counted.add(r.getCode());
        }

        if (weight == 0) {
            return EvaluationNode.leaf(node, Status.NOT_MET, "no matching courses", counted);
        }

        if (node.getAmount().isPresent() && weight < node.getAmount().get() - 1e-9) {
            return EvaluationNode.leaf(node, Status.NOT_MET,
                    weight + " of " + node.getAmount().get() + " credits for average", counted);
        }

        double avg = points / weight;

        if (avg >= node.getMinGrade()) {
            return EvaluationNode.leaf(node, Status.MET, null, counted);
        }

        return EvaluationNode.leaf(node, Status.NOT_MET,
                "average " + String.format(Locale.ROOT, "%.1f", avg) + "%, needs " + node.getMinGrade() + "%", counted);
    }

    @Override
    public EvaluationNode visitGpa(GpaReq node) {
        if (node.getScope().isPresent()) {
            return EvaluationNode.leaf(node, Status.REVIEW,
                    "GPA in " + node.getScope().get() + "; check manually");
        }

        OptionalDouble cgpa = snapshot.getCgpa();

        if (cgpa.isEmpty()) {
            return EvaluationNode.leaf(node, Status.REVIEW,
                    "needs a CGPA of " + node.getMinGpa() + "; CGPA unknown");
        }

        if (cgpa.getAsDouble() >= node.getMinGpa()) {
            return EvaluationNode.leaf(node, Status.MET, null);
        }

        return EvaluationNode.leaf(node, Status.NOT_MET,
                "needs a CGPA of " + node.getMinGpa() + "; you have " + cgpa.getAsDouble());
    }

    @Override
    public EvaluationNode visitProgram(ProgramReq node) {
        if (snapshot.getEnrolledPrograms().stream().anyMatch(node.getPrograms()::contains)) {
            return EvaluationNode.leaf(node, Status.MET, null);
        }

        if (snapshot.getIntendedPrograms().stream().anyMatch(node.getPrograms()::contains)) {
            return EvaluationNode.leaf(node, Status.REVIEW, "you intend to enrol in " + node.getText());
        }

        return EvaluationNode.leaf(node, Status.NOT_MET, "needs " + node.getText());
    }

    @Override
    public EvaluationNode visitRowRef(RowRefReq node) {
        List<Status> rows = new ArrayList<>();

        for (int n : node.getRows()) {
            Optional<Status> s = lists.rowStatus(n);

            if (s.isEmpty()) {
                return EvaluationNode.leaf(node, Status.REVIEW, "checked only in program completion");
            }

            rows.add(s.get());
        }

        Status status = Status.anyOf(rows);

        return EvaluationNode.leaf(node, status, status != Status.MET ? "needs requirement " + node.getRows() : null);
    }

    @Override
    public EvaluationNode visitProfile(ProfileReq node) {
        if (node.getComparison() == ProfileComparison.BY) {
            return EvaluationNode.leaf(node, Status.REVIEW, node.getText());
        }

        Optional<Object> fact = snapshot.profileFact(node.getAttr());

        if (fact.isEmpty()) {
            return EvaluationNode.leaf(node, Status.REVIEW, node.getText());
        }

        boolean ok;

        switch (node.getComparison()) {
            case HAS:
                ok = Boolean.TRUE.equals(fact.get());
                break;
            case MIN:
                ok = ((Number) fact.get()).intValue() >= node.getIntValue();
                break;
            case MAX:
                ok = ((Number) fact.get()).intValue() <= node.getIntValue();
                break;
            default:
                ok = fact.get().equals(node.getValue());
        }

        return ok ? EvaluationNode.leaf(node, Status.MET, null)
                : EvaluationNode.leaf(node, Status.NOT_MET, node.getText());
    }

    @Override
    public EvaluationNode visitText(TextReq node) {
        return EvaluationNode.leaf(node, Status.REVIEW, node.getText());
    }

    private Collection<CourseRecord> available() {
        if (concurrentAllowed) {
            return snapshot.allRecords();
        }

        return new ArrayList<>(snapshot.getTaken().values());
    }

    private Optional<CourseRecord> find(String code) {
        Optional<CourseRecord> r = Optional.ofNullable(snapshot.getTaken().get(code));

        return r.isEmpty() && concurrentAllowed ? Optional.ofNullable(snapshot.getConcurrent().get(code)) : r;
    }

    private List<EvaluationNode> evaluateAll(List<Requirement> items) {
        List<EvaluationNode> evaluations = new ArrayList<>();

        for (Requirement r : items) {
            evaluations.add(evaluate(r));
        }

        return evaluations;
    }

    private List<Status> statuses(List<EvaluationNode> children) {
        return children.stream().map(EvaluationNode::getStatus).collect(Collectors.toList());
    }

    private String notTakenReason(String code) {
        return snapshot.getAsOf()
                .map(term -> code + " not completed before " + term)
                .orElse(code + " not in your plan");
    }
}
