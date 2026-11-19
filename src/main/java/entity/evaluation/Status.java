package entity.evaluation;

import java.util.List;

public enum Status {
    MET, NOT_MET, REVIEW;

    public static Status allOf(List<Status> statuses) {
        if (statuses.contains(NOT_MET)) {
            return NOT_MET;
        } else if (statuses.contains(REVIEW)) {
            return REVIEW;
        }

        return MET;
    }

    public static Status anyOf(List<Status> statuses) {
        if (statuses.contains(MET)) {
            return MET;
        } else if (statuses.contains(REVIEW)) {
            return REVIEW;
        }

        return NOT_MET;
    }

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
