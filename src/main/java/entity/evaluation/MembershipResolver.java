package entity.evaluation;

import java.util.Optional;

public interface MembershipResolver {
    Match onList(String listName, CourseRecord record);
    Match inRowPool(int rowNumber, CourseRecord record);
    Optional<Status> rowStatus(int rowNumber);

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
