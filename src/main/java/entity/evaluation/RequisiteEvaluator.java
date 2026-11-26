package entity.evaluation;

import entity.catalog.Course;
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
            Optional<CourseRecord> transfer = available().stream().filter(t -> t.couldBe(code)).findFirst();

            if (transfer.isPresent()) {
                String t = transfer.get().getCode();

                return EvaluationNode.leaf(node, Status.REVIEW,
                        t + " may count as " + code + " with the department's permission", List.of(t));
            }

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

            double w = node.getWeight().orElse(r.getWeight());
            maybe += w;

            if (m == Match.YES) {
                yes += w * breadthShare(node.getFilter(), r.getCourse());
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
        List<List<CreditsReq>> choices;
        boolean top;

        if (node.getItems().isEmpty()) {
            CreditsReq all = new CreditsReq(node.getAmount().orElse(0.0), null, null, node.getFilter(), false);
            choices = List.of(List.of(all));
            top = node.isBest() && node.getAmount().isPresent();
        } else if (node.getItems().stream().allMatch(RequisiteEvaluator::averageable)) {
            choices = product(node.getItems());
            top = node.isBest();
        } else {
            return EvaluationNode.leaf(node, Status.REVIEW, "average over these courses; check manually");
        }

        List<EvaluationNode> results = new ArrayList<>();

        for (List<CreditsReq> parts : choices) {
            results.add(average(node, parts, top));
        }

        Status s = Status.anyOf(statuses(results));

        return results.stream().filter(n -> n.getStatus() == s).findFirst()
                .orElse(EvaluationNode.leaf(node, Status.NOT_MET, "no matching courses"));
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

    private static double breadthShare(CourseFilter f, Course c) {
        if (f.getBreadth().isEmpty() || c.getBreadth().size() < 2) {
            return 1;
        }

        long matching = c.getBreadth().stream().filter(b -> f.getBreadth().contains(b.getNumber())).count();
        return (double) matching / c.getBreadth().size();
    }

    private EvaluationNode average(AverageReq node, List<CreditsReq> parts, boolean top) {
        Set<String> used = new HashSet<>();
        List<String> counted = new ArrayList<>();
        double weight = 0;
        double points = 0;
        String unknown = null;
        String shortfall = null;

        for (CreditsReq part : parts) {
            List<CourseRecord> graded = new ArrayList<>();

            for (CourseRecord r : available()) {
                Match m = CourseMatcher.matches(part.getFilter(), r, lists);

                if (m == Match.NO || used.contains(r.getCode())) {
                    continue;
                }

                if (m == Match.YES && r.getGrade().isPresent()) {
                    graded.add(r);
                } else if (unknown == null) {
                    unknown = m == Match.MAYBE ? "can't tell if " + r.getCode() + " counts"
                            : "no final grade yet for " + r.getCode();
                }
            }

            if (top) {
                graded.sort(Comparator.comparing((CourseRecord r) -> r.getGrade().get()).reversed());
            }

            double partWeight = 0;

            for (CourseRecord r : graded) {
                if (top && partWeight >= part.getAmount() - 1e-9) {
                    break;
                }

                double w = top ? Math.min(r.getWeight(), part.getAmount() - partWeight) : r.getWeight();
                partWeight += w;
                points += w * r.getGrade().get();
                used.add(r.getCode());
                counted.add(r.getCode());
            }

            weight += partWeight;

            if (shortfall == null && partWeight < part.getAmount() - 1e-9) {
                shortfall = partWeight + " of " + part.getAmount() + " credits for average";
            }
        }

        double avg = points / weight;

        if (shortfall == null && weight > 0 && avg >= node.getMinGrade() && (top || unknown == null)) {
            return EvaluationNode.leaf(node, Status.MET, null, counted);
        }

        if (unknown != null) {
            return EvaluationNode.leaf(node, Status.REVIEW, unknown, counted);
        }

        if (weight == 0) {
            return EvaluationNode.leaf(node, Status.NOT_MET, "no matching courses", counted);
        }

        if (shortfall != null) {
            return EvaluationNode.leaf(node, Status.NOT_MET, shortfall, counted);
        }

        return EvaluationNode.leaf(node, Status.NOT_MET,
                "average " + String.format(Locale.ROOT, "%.1f", avg) + "%, needs " + node.getMinGrade() + "%", counted);
    }

    private static boolean averageable(Requirement r) {
        if (r instanceof OrReq) {
            return allCredits(((OrReq) r).getItems());
        }

        if (r instanceof ChooseReq) {
            return allCredits(((ChooseReq) r).getItems());
        }

        return r instanceof CreditsReq;
    }

    private static boolean allCredits(List<Requirement> items) {
        return items.stream().allMatch(i -> i instanceof CreditsReq);
    }

    private static List<List<CreditsReq>> product(List<Requirement> items) {
        List<List<CreditsReq>> result = List.of(List.of());

        for (Requirement item : items) {
            List<List<CreditsReq>> next = new ArrayList<>();

            for (List<CreditsReq> prefix : result) {
                for (List<CreditsReq> option : options(item)) {
                    List<CreditsReq> joined = new ArrayList<>(prefix);
                    joined.addAll(option);
                    next.add(joined);
                }
            }

            result = next;
        }

        return result;
    }

    private static List<List<CreditsReq>> options(Requirement item) {
        if (item instanceof OrReq) {
            return combinations(((OrReq) item).getItems(), 1);
        }

        if (item instanceof ChooseReq) {
            return combinations(((ChooseReq) item).getItems(), ((ChooseReq) item).getCount());
        }

        return List.of(List.of((CreditsReq) item));
    }

    private static List<List<CreditsReq>> combinations(List<Requirement> items, int count) {
        if (count == 0) {
            return List.of(List.of());
        }

        if (items.size() < count) {
            return List.of();
        }

        List<Requirement> rest = items.subList(1, items.size());
        List<List<CreditsReq>> result = new ArrayList<>();

        for (List<CreditsReq> c : combinations(rest, count - 1)) {
            List<CreditsReq> with = new ArrayList<>();
            with.add((CreditsReq) items.get(0));
            with.addAll(c);
            result.add(with);
        }

        result.addAll(combinations(rest, count));

        return result;
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
