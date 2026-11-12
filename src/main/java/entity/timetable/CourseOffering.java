package entity.timetable;

import entity.term.Placement;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class CourseOffering {
    private final String courseCode;
    private final Placement placement;
    private final List<Activity> activities;

    public CourseOffering(String courseCode, Placement placement, List<Activity> activities) {
        this.courseCode = Objects.requireNonNull(courseCode, "courseCode");
        this.placement = Objects.requireNonNull(placement, "placement");
        this.activities = List.copyOf(Objects.requireNonNull(activities, "activities"));
    }

    public String getCourseCode() {
        return courseCode;
    }

    public Placement getPlacement() {
        return placement;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public Optional<Activity> activity(String code) {
        return activities.stream().filter(a -> a.getCode().equals(code)).findFirst();
    }

    public Set<ActivityType> activityTypes() {
        return activities.stream().map(Activity::getType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ActivityType.class)));
    }

    public List<Activity> activitiesOf(ActivityType type) {
        return activities.stream().filter(a -> a.getType() == type).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseOffering)) {
            return false;
        }
        CourseOffering that = (CourseOffering) o;
        return courseCode.equals(that.courseCode) && placement.equals(that.placement)
                && activities.equals(that.activities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode, placement, activities);
    }

    @Override
    public String toString() {
        return "CourseOffering{" + courseCode + ", " + placement + ", " + activities + "}";
    }
}
