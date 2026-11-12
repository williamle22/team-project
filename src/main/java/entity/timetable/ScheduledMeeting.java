package entity.timetable;

import java.util.Objects;

public final class ScheduledMeeting {
    private final String courseCode;
    private final String activityCode;
    private final ActivityType type;
    private final MeetingTime time;

    public ScheduledMeeting(String courseCode, String activityCode, ActivityType type, MeetingTime time) {
        this.courseCode = Objects.requireNonNull(courseCode, "courseCode");
        this.activityCode = Objects.requireNonNull(activityCode, "activityCode");
        this.type = Objects.requireNonNull(type, "type");
        this.time = Objects.requireNonNull(time, "time");
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getActivityCode() {
        return activityCode;
    }

    public ActivityType getType() {
        return type;
    }

    public MeetingTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScheduledMeeting)) {
            return false;
        }
        ScheduledMeeting that = (ScheduledMeeting) o;
        return courseCode.equals(that.courseCode) && activityCode.equals(that.activityCode) && type == that.type
                && time.equals(that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode, activityCode, type, time);
    }

    @Override
    public String toString() {
        return "ScheduledMeeting{" + courseCode + " " + activityCode + ", " + time + "}";
    }
}
