package entity.catalog;

/** A breadth requirement category, numbered 1-5. */
public enum BreadthCategory {
    CREATIVE_CULTURAL(1, "Creative and Cultural Representations"),
    THOUGHT_BELIEF(2, "Thought, Belief and Behaviour"),
    SOCIETY_INSTITUTIONS(3, "Society and its Institutions"),
    LIVING_THINGS(4, "Living Things and Their Environment"),
    PHYSICAL_MATHEMATICAL(5, "The Physical and Mathematical Universes");

    private final int number;
    private final String displayName;

    BreadthCategory(int number, String displayName) {
        this.number = number;
        this.displayName = displayName;
    }

    public int getNumber() {
        return this.number;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static BreadthCategory fromInt(int number) throws IllegalArgumentException {
        for (BreadthCategory b : values()) {
            if (b.number == number) {
                return b;
            }
        }
        throw new IllegalArgumentException("unknown breadth category: " + number);
    }

    /** Returns the display label shown in the UI, such as {@code "BR 1: Creative and Cultural Representations"}. */
    @Override
    public String toString() {
        return "BR " + this.number + ": " + this.displayName;
    }
}
