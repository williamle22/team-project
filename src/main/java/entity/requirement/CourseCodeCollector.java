package entity.requirement;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A visitor that collects every course code a requirement tree mentions, in tree order without duplicates.
 *
 * <p>It reads {@code course} nodes and the {@code courses} lists of {@code credits} and {@code average} filters. It
 * descends through {@code and}, {@code or}, {@code choose}, {@code not}, and {@code average} items. Other nodes
 * contribute nothing.
 */
public final class CourseCodeCollector implements RequirementVisitor<Set<String>> {

    @Override
    public Set<String> visitAnd(AndReq node) {
        return collectAll(node.getItems());
    }

    @Override
    public Set<String> visitOr(OrReq node) {
        return collectAll(node.getItems());
    }

    @Override
    public Set<String> visitChoose(ChooseReq node) {
        return collectAll(node.getItems());
    }

    @Override
    public Set<String> visitNot(NotReq node) {
        return node.getItem().accept(this);
    }

    @Override
    public Set<String> visitCourse(CourseReq node) {
        Set<String> codes = new LinkedHashSet<>();
        codes.add(node.getCode());
        return codes;
    }

    @Override
    public Set<String> visitCredits(CreditsReq node) {
        return new LinkedHashSet<>(node.getFilter().getCourses());
    }

    @Override
    public Set<String> visitAverage(AverageReq node) {
        Set<String> codes = new LinkedHashSet<>(node.getFilter().getCourses());
        codes.addAll(collectAll(node.getItems()));
        return codes;
    }

    @Override
    public Set<String> visitGpa(GpaReq node) {
        return new LinkedHashSet<>();
    }

    @Override
    public Set<String> visitProgram(ProgramReq node) {
        return new LinkedHashSet<>();
    }

    @Override
    public Set<String> visitRowRef(RowRefReq node) {
        return new LinkedHashSet<>();
    }

    @Override
    public Set<String> visitProfile(ProfileReq node) {
        return new LinkedHashSet<>();
    }

    @Override
    public Set<String> visitText(TextReq node) {
        return new LinkedHashSet<>();
    }

    private Set<String> collectAll(List<? extends Requirement> items) {
        Set<String> codes = new LinkedHashSet<>();
        for (Requirement item : items) {
            codes.addAll(item.accept(this));
        }
        return codes;
    }
}
