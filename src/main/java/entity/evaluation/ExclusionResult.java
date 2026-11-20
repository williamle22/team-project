package entity.evaluation;

public enum ExclusionResult {
    EXCLUDED, NOT_EXCLUDED, REVIEW;

    public Status toStatus() {
        switch (this) {
            case EXCLUDED:
                return Status.NOT_MET;
            case REVIEW:
                return Status.REVIEW;
            default:
                return Status.MET;
        }
    }
}
