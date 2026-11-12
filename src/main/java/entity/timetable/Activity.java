package entity.timetable;

import java.util.List;
import java.util.Objects;

// TODO: EASI fields not yet used:
//    - Sections: deliveryModes (INPER, SYNC, ASYNC), currentEnrolment, maxEnrolment, currentWaitlist, cancelInd, notes, enrolmentControls
//    - Meeting Times: repetition, buildingURL

public final class Activity {
    private final String code;
    private final ActivityType type;
    private final List<MeetingTime> meetings;
    private final List<String> instructors;

    public Activity(String code, ActivityType type, List<MeetingTime> meetings, List<String> instructors) {
        this.code = Objects.requireNonNull(code, "code");
        this.type = Objects.requireNonNull(type, "type");
        this.meetings = List.copyOf(Objects.requireNonNull(meetings, "meetings"));
        this.instructors = List.copyOf(Objects.requireNonNull(instructors, "instructors"));
    }

    public String getCode() {
        return code;
    }

    public ActivityType getType() {
        return type;
    }

    public List<MeetingTime> getMeetings() {
        return meetings;
    }

    public List<String> getInstructors() {
        return instructors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Activity)) {
            return false;
        }
        Activity that = (Activity) o;
        return code.equals(that.code) && type == that.type && meetings.equals(that.meetings)
                && instructors.equals(that.instructors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, type, meetings, instructors);
    }

    @Override
    public String toString() {
        return "Activity{" + code + ", " + meetings + "}";
    }
}
