package entity.catalog;

import entity.requirement.Requirement;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The completion requirements of a program, from its {@code completion} JSON: numbered rows, named course lists,
 * and exclusions. A row can name a parent that doesn't exist. The evaluator reports that row.
 */
public final class Completion {
    private final List<CompletionRow> requirements;
    private final List<CourseList> lists;
    private final List<Requirement> exclusions;
    private final List<String> notes;
    private final String raw;

    /** @throws IllegalArgumentException if two rows share a number or two lists share a name */
    public Completion(List<CompletionRow> requirements, List<CourseList> lists,
                      List<Requirement> exclusions, List<String> notes, String raw) {
        this.requirements = List.copyOf(Objects.requireNonNull(requirements, "requirements"));
        this.lists = List.copyOf(Objects.requireNonNull(lists, "lists"));
        this.exclusions = List.copyOf(Objects.requireNonNull(exclusions, "exclusions"));
        this.notes = List.copyOf(Objects.requireNonNull(notes, "notes"));
        this.raw = Objects.requireNonNull(raw, "raw");

        Set<Integer> numbers = new HashSet<>();
        for (CompletionRow row : this.requirements) {
            if (!numbers.add(row.getNumber())) {
                throw new IllegalArgumentException("duplicate row number " + row.getNumber());
            }
        }
        Set<String> names = new HashSet<>();
        for (CourseList list : this.lists) {
            if (!names.add(list.getName())) {
                throw new IllegalArgumentException("duplicate list name " + list.getName());
            }
        }
    }

    /** Returns the rows in the order the backend lists them, which is by number. */
    public List<CompletionRow> getRequirements() {
        return requirements;
    }

    public List<CourseList> getLists() {
        return lists;
    }

    /** Returns requirements that describe programs a student can't combine with this one. Some might need review. */
    public List<Requirement> getExclusions() {
        return exclusions;
    }

    public List<String> getNotes() {
        return notes;
    }

    public String getRaw() {
        return raw;
    }

    public Optional<CompletionRow> row(int number) {
        return requirements.stream().filter(r -> r.getNumber() == number).findFirst();
    }

    /** Returns the rows whose parent is row {@code number}, in list order. */
    public List<CompletionRow> childrenOf(int number) {
        return requirements.stream()
                .filter(r -> r.getParent().equals(Optional.of(number)))
                .collect(Collectors.toUnmodifiableList());
    }

    /** Returns the rows without a parent, in list order. */
    public List<CompletionRow> topLevelRows() {
        return requirements.stream().filter(CompletionRow::isTopLevel).collect(Collectors.toUnmodifiableList());
    }

    public Optional<CourseList> list(String name) {
        return lists.stream().filter(l -> l.getName().equals(name)).findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Completion)) {
            return false;
        }
        Completion that = (Completion) o;
        return requirements.equals(that.requirements)
                && lists.equals(that.lists)
                && exclusions.equals(that.exclusions)
                && notes.equals(that.notes)
                && raw.equals(that.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirements, lists, exclusions, notes, raw);
    }

    @Override
    public String toString() {
        return "Completion{requirements=" + requirements + ", lists=" + lists + ", exclusions=" + exclusions + "}";
    }
}
