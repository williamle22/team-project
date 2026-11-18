package entity.timetable;

import entity.term.AcademicTerm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A list of scheduled courses. Its methods find the meetings in each term, time conflicts, and missing or stale
 * choices.
 */
public final class Timetable {
    private final List<ScheduledCourse> courses;

    public Timetable(List<ScheduledCourse> courses) {
        this.courses = List.copyOf(Objects.requireNonNull(courses, "courses"));
    }

    public List<ScheduledCourse> getCourses() {
        return courses;
    }

    /** Returns every term that a course's placement covers, sorted and without duplicates. */
    public List<AcademicTerm> termsCovered() {
        return courses.stream()
                .flatMap(c -> c.getOffering().getPlacement().terms().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns the meetings of the chosen activities in {@code term}. A Y course appears in both of its terms through
     * its per-term meetings.
     */
    public List<ScheduledMeeting> meetingsIn(AcademicTerm term) {
        List<ScheduledMeeting> meetings = new ArrayList<>();
        for (ScheduledCourse c : courses) {
            for (Activity a : c.chosenActivities()) {
                for (MeetingTime m : a.getMeetings()) {
                    if (m.getTerm().equals(term)) {
                        meetings.add(new ScheduledMeeting(c.getOffering().getCourseCode(), a.getCode(), a.getType(), m));
                    }
                }
            }
        }
        return meetings;
    }

    /**
     * Returns every pair of overlapping meetings within each term, including two activities of the same course. If two
     * Y-course meetings clash in both terms, the result has one conflict for each term.
     */
    public List<Conflict> conflicts() {
        List<Conflict> conflicts = new ArrayList<>();
        for (AcademicTerm term : termsCovered()) {
            List<ScheduledMeeting> meetings = meetingsIn(term);
            for (int i = 0; i < meetings.size(); i++) {
                for (int j = i + 1; j < meetings.size(); j++) {
                    if (meetings.get(i).getTime().overlaps(meetings.get(j).getTime())) {
                        conflicts.add(new Conflict(meetings.get(i), meetings.get(j)));
                    }
                }
            }
        }
        return conflicts;
    }

    /**
     * Returns each course's {@link ScheduledCourse#missingTypes()}, keyed by course code in course order. The map
     * omits courses with no missing types.
     */
    public Map<String, Set<ActivityType>> missingChoices() {
        Map<String, Set<ActivityType>> missing = new LinkedHashMap<>();
        for (ScheduledCourse c : courses) {
            Set<ActivityType> types = c.missingTypes();
            if (!types.isEmpty()) {
                missing.put(c.getOffering().getCourseCode(), types);
            }
        }
        return missing;
    }

    /**
     * Returns each course's {@link ScheduledCourse#staleChoices()}, keyed by course code in course order. The map
     * omits courses with no stale choices.
     */
    public Map<String, Map<ActivityType, String>> staleChoices() {
        Map<String, Map<ActivityType, String>> stale = new LinkedHashMap<>();
        for (ScheduledCourse c : courses) {
            Map<ActivityType, String> choices = c.staleChoices();
            if (!choices.isEmpty()) {
                stale.put(c.getOffering().getCourseCode(), choices);
            }
        }
        return stale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Timetable)) {
            return false;
        }
        return courses.equals(((Timetable) o).courses);
    }

    @Override
    public int hashCode() {
        return courses.hashCode();
    }

    @Override
    public String toString() {
        return "Timetable" + courses;
    }
}
