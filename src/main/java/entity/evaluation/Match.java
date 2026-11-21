package entity.evaluation;

import java.util.List;

/**
 * Whether a course matches a filter. {@code MAYBE} means the app can't tell, such as when the filter needs a grade that
 * the course doesn't have yet.
 */
public enum Match {
    YES, NO, MAYBE;

    /**
     * Returns {@code NO} if any match is {@code NO}, otherwise {@code MAYBE} if any is {@code MAYBE}, otherwise {@code
     * YES}.
     */
    public static Match allOf(List<Match> matches) {
        if (matches.contains(NO)) {
            return NO;
        } else if (matches.contains(MAYBE)) {
            return MAYBE;
        }

        return YES;
    }

    /**
     * Returns {@code YES} if any match is {@code YES}, otherwise {@code MAYBE} if any is {@code MAYBE}, otherwise
     * {@code NO}.
     */
    public static Match anyOf(List<Match> matches) {
        if (matches.contains(YES)) {
            return YES;
        } else if (matches.contains(MAYBE)) {
            return MAYBE;
        }

        return NO;
    }
}
