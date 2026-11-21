package entity.evaluation;

import java.util.Optional;

/**
 * Answers what a requirement can't answer on its own: whether a course is on a named course list or in a program
 * completion row's pool, and how a completion row evaluated.
 */
public interface MembershipResolver {
    Match onList(String listName, CourseRecord record);
    Match inRowPool(int rowNumber, CourseRecord record);
    /** Returns the status of completion row {@code rowNumber}, or empty if this resolver doesn't evaluate rows. */
    Optional<Status> rowStatus(int rowNumber);

    /**
     * Answers {@code MAYBE} for every membership and empty for every row. Course requisites use it, because they aren't
     * part of a program.
     */
    MembershipResolver NONE = new MembershipResolver() {
        @Override
        public Match onList(String listName, CourseRecord record) {
            return Match.MAYBE;
        }

        @Override
        public Match inRowPool(int rowNumber, CourseRecord record) {
            return Match.MAYBE;
        }

        @Override
        public Optional<Status> rowStatus(int rowNumber) {
            return Optional.empty();
        }
    };
}
