package entity.timetable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class TimetableChoice {
    private final Map<ActivityType, String> chosen;

    public TimetableChoice(Map<ActivityType, String> chosen) {
        this.chosen = Map.copyOf(Objects.requireNonNull(chosen, "chosen"));
    }

    public static TimetableChoice empty() {
        return new TimetableChoice(Map.of());
    }

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
