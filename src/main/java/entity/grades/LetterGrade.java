package entity.grades;

import java.util.Arrays;

/**
 * The University of Toronto Faculty of Arts and Science grade scale, highest grade first. Each grade has a minimum
 * percent and grade points on a 0-4 scale.
 *
 * @see <a href="https://artsci.calendar.utoronto.ca/academic-record">Grading and Course Marks</a>
 */
public enum LetterGrade {
    A_PLUS(90, 4.0, "A+"),
    A(85, 4.0, "A"),
    A_MINUS(80, 3.7, "A-"),
    B_PLUS(77, 3.3, "B+"),
    B(73, 3.0, "B"),
    B_MINUS(70, 2.7, "B-"),
    C_PLUS(67, 2.3, "C+"),
    C(63, 2.0, "C"),
    C_MINUS(60, 1.7, "C-"),
    D_PLUS(57, 1.3, "D+"),
    D(53, 1.0, "D"),
    D_MINUS(50, 0.7, "D-"),
    F(0, 0.0, "F");

    private final int minPercent;
    private final double points;
    private final String display;

    LetterGrade(int minPercent, double points, String display) {
        this.minPercent = minPercent;
        this.points = points;
        this.display = display;
    }

    public int getMinPercent() {
        return minPercent;
    }

    public double getPoints() {
        return points;
    }

    public String getDisplay() {
        return display;
    }

    /** Returns whether this grade passes. Only {@code F}, below 50, fails. */
    public boolean isPassing() {
        return minPercent >= 50;
    }

    /**
     * Returns the letter grade for {@code percent}.
     *
     * @throws IllegalArgumentException if {@code percent} isn't 0-100
     */
    public static LetterGrade fromPercent(int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("percent must be 0-100, was " + percent);
        }
        return Arrays.stream(values()).filter(g -> percent >= g.minPercent).findFirst().orElseThrow();
    }
}
