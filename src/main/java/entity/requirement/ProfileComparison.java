package entity.requirement;

/**
 * How a {@code profile} node compares a fact about the student with its value. {@link #forAttribute} derives the
 * comparison from the attribute name:
 * <ul>
 *   <li>{@link #HAS}: prefix {@code has_}. The fact is true.
 *   <li>{@link #MIN}: prefix {@code min_}. The fact is at least the value.
 *   <li>{@link #MAX}: prefix {@code max_}. The fact is at most the value.
 *   <li>{@link #BY}: suffix {@code _by}. The value is a deadline. A planner can't check it, so it needs manual review.
 *   <li>{@link #EQUALS}: any other name. The fact equals the value.
 * </ul>
 * Prefixes take precedence over the suffix.
 */
public enum ProfileComparison {
    EQUALS, HAS, MIN, MAX, BY;

    public static ProfileComparison forAttribute(String attr) {
        if (attr.startsWith("has_")) return HAS;
        if (attr.startsWith("min_")) return MIN;
        if (attr.startsWith("max_")) return MAX;
        if (attr.endsWith("_by")) return BY;
        return EQUALS;
    }
}
