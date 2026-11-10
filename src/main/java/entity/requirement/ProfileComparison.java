package entity.requirement;

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
