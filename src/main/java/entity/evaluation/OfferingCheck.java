package entity.evaluation;

import entity.catalog.Course;
import entity.catalog.Sessions;
import entity.term.Placement;

import java.util.Optional;

public final class OfferingCheck {
    private OfferingCheck() {}

    /**
     * Returns whether {@code course} runs in {@code placement}'s period and section. A course with no session data is
     * {@code NOT_OFFERED}. Outside the current academic year, returns {@code NOT_APPLICABLE}.
     */
    public static OfferingStatus check(Course course, Placement placement, boolean currentAcademicYear) {
        if (!currentAcademicYear) {
            return OfferingStatus.NOT_APPLICABLE;
        }

        Optional<Sessions> sessions = course.getSessions();

        if (sessions.isEmpty()) {
            return OfferingStatus.NOT_OFFERED;
        }

        return sessions.get().offers(placement.getPeriod(), placement.getSection())
                ? OfferingStatus.OFFERED : OfferingStatus.NOT_OFFERED;
    }
}
