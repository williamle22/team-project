package entity.timetable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The activity code a student chooses for each activity type of one course, such as
 * {@code {LEC: "LEC0101", TUT: "TUT0201"}}. This class is immutable: {@link #with} and {@link #without} return
 * copies. The iteration order of {@link #asMap()} and {@link #chosenTypes()} is unspecified. To get a fixed order,
 * loop over {@link ActivityType#values()}.
 */
public final class TimetableChoice {
    private final Map<ActivityType, String> chosen;

    public TimetableChoice(Map<ActivityType, String> chosen) {
        this.chosen = Map.copyOf(Objects.requireNonNull(chosen, "chosen"));
    }

    public static TimetableChoice empty() {
        return new TimetableChoice(Map.of());
    }

    /** Returns the chosen activity code for {@code type}, or empty if none is chosen. */
    public Optional<String> get(ActivityType type) {
        return Optional.ofNullable(chosen.get(type));
    }

    public Set<ActivityType> chosenTypes() {
        return chosen.keySet();
    }

    public Map<ActivityType, String> asMap() {
        return chosen;
    }

    public TimetableChoice with(ActivityType type, String activityCode) {
        Map<ActivityType, String> map = new HashMap<>(chosen);
        map.put(type, activityCode);
        return new TimetableChoice(map);
    }

    public TimetableChoice without(ActivityType type) {
        Map<ActivityType, String> map = new HashMap<>(chosen);
        map.remove(type);
        return new TimetableChoice(map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimetableChoice)) {
            return false;
        }
        TimetableChoice that = (TimetableChoice) o;
        return chosen.equals(that.chosen);
    }

    @Override
    public int hashCode() {
        return chosen.hashCode();
    }

    @Override
    public String toString() {
        return "TimetableChoice" + chosen;
    }
}
