package entity.catalog;

import entity.requirement.Requirement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** A named course list, which course filters reference by {@link #getName()}. */
public final class CourseList {
    private final String name;
    private final String title;
    private final List<Requirement> items;

    public CourseList(String name, String title, List<Requirement> items) {
        this.name = Objects.requireNonNull(name, "name");
        this.title = title;
        this.items = List.copyOf(Objects.requireNonNull(items, "items"));
    }

    public String getName() {
        return name;
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    /** Returns the requirement nodes that hold the list's courses. */
    public List<Requirement> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseList)) {
            return false;
        }
        CourseList that = (CourseList) o;
        return name.equals(that.name) && Objects.equals(title, that.title) && items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, title, items);
    }

    @Override
    public String toString() {
        return "CourseList{name=" + name + ", title=" + title + ", items=" + items + "}";
    }
}
