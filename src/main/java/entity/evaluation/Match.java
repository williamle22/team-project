package entity.evaluation;

import java.util.List;

public enum Match {
    YES, NO, MAYBE;

    public static Match allOf(List<Match> matches) {
        if (matches.contains(NO)) {
            return NO;
        } else if (matches.contains(MAYBE)) {
            return MAYBE;
        }

        return YES;
    }

    public static Match anyOf(List<Match> matches) {
        if (matches.contains(YES)) {
            return YES;
        } else if (matches.contains(MAYBE)) {
            return MAYBE;
        }

        return NO;
    }
}
