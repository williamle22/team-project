package entity.evaluation;

/** Whether a course is excluded. {@code REVIEW} means an exclusion depends on something the app can't check. */
public enum ExclusionResult {
    EXCLUDED, NOT_EXCLUDED, REVIEW;

    /**
     * Returns the matching requirement status: {@code EXCLUDED} gives {@code NOT_MET}, {@code REVIEW} gives {@code
     * REVIEW}, and {@code NOT_EXCLUDED} gives {@code MET}.
     */
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
