package entity.evaluation;

import java.util.List;

/**
 * The result of checking a requirement. {@code REVIEW} means the app can't decide, so the student checks it manually.
 */
public enum Status {
    MET, NOT_MET, REVIEW;

    /**
     * Returns {@code NOT_MET} if any status is {@code NOT_MET}, otherwise {@code REVIEW} if any is {@code REVIEW},
     * otherwise {@code MET}. An empty list gives {@code MET}.
     */
    public static Status allOf(List<Status> statuses) {
        if (statuses.contains(NOT_MET)) {
            return NOT_MET;
        } else if (statuses.contains(REVIEW)) {
            return REVIEW;
        }

        return MET;
    }

    /**
     * Returns {@code MET} if any status is {@code MET}, otherwise {@code REVIEW} if any is {@code REVIEW}, otherwise
     * {@code NOT_MET}. An empty list gives {@code NOT_MET}.
     */
    public static Status anyOf(List<Status> statuses) {
        if (statuses.contains(MET)) {
            return MET;
        } else if (statuses.contains(REVIEW)) {
            return REVIEW;
        }

        return NOT_MET;
    }

    /**
     * Returns {@code MET} if at least {@code count} statuses are {@code MET}, {@code REVIEW} if the {@code MET} and
     * {@code REVIEW} statuses together reach {@code count}, and {@code NOT_MET} otherwise.
     */
    public static Status atLeast(int count, List<Status> statuses) {
        int metCount = 0;
        int reviewCount = 0;

        for (Status s : statuses) {
            if (s == MET) {
                metCount++;
            } else if (s == REVIEW) {
                reviewCount++;
            }
        }

        if (metCount >= count) {
            return MET;
        } else if (metCount + reviewCount >= count) {
            return REVIEW;
        }

        return NOT_MET;
    }

    /** Swaps {@code MET} and {@code NOT_MET}. {@code REVIEW} stays {@code REVIEW}. */
    public Status negate() {
        switch (this) {
            case MET:
                return NOT_MET;
            case NOT_MET:
                return MET;
            default:
                return REVIEW;
        }
    }
}
