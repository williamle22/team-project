package entity.evaluation;

/**
 * Whether a course runs in a placement. {@code NOT_APPLICABLE} means the placement isn't in the current academic year,
 * so the calendar can't say.
 */
public enum OfferingStatus {
    OFFERED, NOT_OFFERED, NOT_APPLICABLE;
}
