package entity.requirement;

import java.util.List;
import java.util.stream.Collectors;

public final class RequirementPrinter implements RequirementVisitor<String> {

    public String print(Requirement r) {
        return r.accept(this) + (r.isConcurrent() ? " (may be taken concurrently)" : "");
    }

    @Override
    public String visitAnd(AndReq node) {
        return "(" + printAll(node.getItems(), " and ") + ")";
    }

    @Override
    public String visitOr(OrReq node) {
        return "(" + printAll(node.getItems(), " or ") + ")";
    }

    @Override
    public String visitChoose(ChooseReq node) {
        return node.getCount() + " of (" + printAll(node.getItems(), ", ") + ")";
    }

    @Override
    public String visitNot(NotReq node) {
        return "not " + print(node.getItem());
    }

    @Override
    public String visitCourse(CourseReq node) {
        return node.getCode() + node.getMinGrade().map(g -> " (min. " + g + "%)").orElse("");
    }

    @Override
    public String visitCredits(CreditsReq node) {
        if (node.getMaxAmount().isPresent() && node.getAmount() == 0) {
            return "at most " + credits(node.getMaxAmount().get()) + " credits" + describe(node.getFilter());
        }
        return credits(node.getAmount()) + " credits" + describe(node.getFilter());
    }

    @Override
    public String visitAverage(AverageReq node) {
        return "an average of at least " + node.getMinGrade() + "%" + describe(node.getFilter())
                + node.getAmount().map(a -> " over " + credits(a) + " credits").orElse("");
    }

    @Override
    public String visitGpa(GpaReq node) {
        return "a GPA of at least " + String.format("%.2f", node.getMinGpa())
                + node.getScope().map(s -> " (" + s + ")").orElse("");
    }

    @Override
    public String visitProgram(ProgramReq node) {
        return node.getText();
    }

    @Override
    public String visitRowRef(RowRefReq node) {
        List<Integer> rows = node.getRows();
        if (rows.size() == 1) {
            return "Requirement " + rows.get(0);
        }
        return "Requirements " + join(rows.subList(0, rows.size() - 1), ", ") + " or " + rows.get(rows.size() - 1);
    }

    @Override
    public String visitProfile(ProfileReq node) {
        return node.getText();
    }

    @Override
    public String visitText(TextReq node) {
        return node.getText();
    }

    private String printAll(List<? extends Requirement> items, String separator) {
        return items.stream().map(this::print).collect(Collectors.joining(separator));
    }

    private static String credits(double amount) {
        return String.format("%.1f", amount);
    }

    private static String join(List<?> values, String separator) {
        return values.stream().map(String::valueOf).collect(Collectors.joining(separator));
    }

    private static String describe(CourseFilter f) {
        StringBuilder sb = new StringBuilder();
        if (!f.getCourses().isEmpty()) {
            sb.append(" from ").append(join(f.getCourses(), ", "));
        }
        if (!f.getDesignators().isEmpty()) {
            sb.append(" in ").append(join(f.getDesignators(), "/"));
        }
        if (!f.getLevels().isEmpty()) {
            sb.append(" at the ").append(join(f.getLevels(), "/")).append(" level");
        }
        if (!f.getBreadth().isEmpty()) {
            sb.append(" in breadth ").append(join(f.getBreadth(), "/"));
        }
        if (!f.getLists().isEmpty()) {
            sb.append(" from ").append(join(f.getLists(), ", "));
        }
        if (!f.getExclude().isEmpty()) {
            sb.append(" excluding ").append(join(f.getExclude(), ", "));
        }
        f.getMinGrade().ifPresent(g -> sb.append(" with at least ").append(g).append("%"));
        return sb.toString();
    }
}
