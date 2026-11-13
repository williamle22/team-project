package entity.timetable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** A course offering plus the student's choice of its sections. */
public final class ScheduledCourse {
    private final CourseOffering offering;
    private final TimetableChoice choice;

    public ScheduledCourse(CourseOffering offering, TimetableChoice choice) {
        this.offering = Objects.requireNonNull(offering, "offering");
        this.choice = Objects.requireNonNull(choice, "choice");
    }

    public CourseOffering getOffering() {
        return offering;
    }

    public TimetableChoice getChoice() {
        return choice;
    }

    /** Returns the chosen activities, in offering order. */
    public List<Activity> chosenActivities() {
        return offering.getActivities().stream()
                .filter(a -> choice.get(a.getType()).equals(Optional.of(a.getCode())))
                .collect(Collectors.toUnmodifiableList());
    }

    /** Returns the activity types that the offering has but the choice doesn't cover. */
    public Set<ActivityType> missingTypes() {
        Set<ActivityType> missing = offering.activityTypes();
        missing.removeAll(choice.chosenTypes());
        return missing;
    }

    /** Returns the chosen codes that the offering no longer has, or that belong to an activity of a different type. */
    public Map<ActivityType, String> staleChoices() {
        Map<ActivityType, String> stale = new HashMap<>();
        for (Map.Entry<ActivityType, String> e : choice.asMap().entrySet()) {
            boolean offered = offering.activity(e.getValue()).filter(a -> a.getType() == e.getKey()).isPresent();
            if (!offered) {
                stale.put(e.getKey(), e.getValue());
            }
        }
        return stale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScheduledCourse)) {
            return false;
        }
        ScheduledCourse that = (ScheduledCourse) o;
        return offering.equals(that.offering) && choice.equals(that.choice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offering, choice);
    }

    @Override
    public String toString() {
        return "ScheduledCourse{" + offering.getCourseCode() + ", " + choice + "}";
    }
}
