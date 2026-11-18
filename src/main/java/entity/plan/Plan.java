package entity.plan;

import entity.degree.DegreeType;
import entity.term.AcademicTerm;
import entity.term.Placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A student's degree plan: its degree, programs, terms, courses, and transfer credits. A plan is the mutable root of
 * this data, so change it only through its methods. Getters return read-only views. Terms stay sorted.
 *
 * <p>A plan enforces only these rules:
 * <ul>
 *   <li>Every term that a course's placement occupies is in the plan.
 *   <li>The same code doesn't appear twice in the same placement. The same code in a different placement is a retake,
 *       which is allowed.
 *   <li>Terms, program codes, transfer credit codes, and course ids are unique.
 *   <li>A term cannot be removed if it still contains courses.
 *   <li>The name isn't {@code null} or blank.
 * </ul>
 * Removing a term that has courses throws {@code IllegalStateException}. Breaking any other rule, or passing an
 * unknown id, code, or term, throws {@code IllegalArgumentException}.
 * Evaluation checks everything else, such as prerequisites, exclusions, offerings, and retakes, so students can always
 * build the plan they want and see warnings.
 *
 * <p>A plan is mutable, and its hash code changes when you edit it. Don't use a plan as a {@code HashMap} key or
 * {@code HashSet} element.
 */
public final class Plan {
    private final UUID id;
    private String name;
    private DegreeType degree;
    private final List<ProgramSelection> programs = new ArrayList<>();
    private final List<AcademicTerm> terms = new ArrayList<>();
    private final List<PlannedCourse> courses = new ArrayList<>();
    private final List<TransferCredit> transferCredits = new ArrayList<>();

    private Plan(UUID id, String name) {
        this.id = Objects.requireNonNull(id, "id");
        rename(name);
    }

    /**
     * Creates an empty plan with a new random id.
     *
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank
     */
    public static Plan create(String name) {
        return new Plan(UUID.randomUUID(), name);
    }

    /**
     * Rebuilds a saved plan by replaying the add methods, so the plan's rules apply.
     *
     * @throws IllegalArgumentException if the saved data breaks a rule, such as a course in a term the plan doesn't
     *     have
     */
    public static Plan restore(UUID id, String name, DegreeType degree,
                               List<ProgramSelection> programs, List<AcademicTerm> terms,
                               List<PlannedCourse> courses, List<TransferCredit> transferCredits) {
        Plan plan = new Plan(id, name);
        plan.setDegree(degree);
        for (ProgramSelection p : programs) {
            plan.addProgram(p.getCode(), p.getStatus());
        }
        for (AcademicTerm t : terms) {
            plan.addTerm(t);
        }
        for (PlannedCourse c : courses) {
            plan.insertCourse(c);
        }
        for (TransferCredit t : transferCredits) {
            plan.addTransferCredit(t);
        }
        return plan;
    }

    /** Returns an independent copy with the same id. */
    public Plan copy() {
        return restore(id, name, degree, programs, terms, courses, transferCredits);
    }

    /**
     * Returns an independent copy with a new random id. Use it to duplicate a plan or to resolve an id collision on
     * import.
     */
    public Plan copyWithNewId() {
        return restore(UUID.randomUUID(), name, degree, programs, terms, courses, transferCredits);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Returns the degree type, or empty if none is set. */
    public Optional<DegreeType> getDegree() {
        return Optional.ofNullable(degree);
    }

    public List<ProgramSelection> getPrograms() {
        return Collections.unmodifiableList(programs);
    }

    public List<AcademicTerm> getTerms() {
        return Collections.unmodifiableList(terms);
    }

    public List<PlannedCourse> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    public List<TransferCredit> getTransferCredits() {
        return Collections.unmodifiableList(transferCredits);
    }

    /** Returns the entry with this id, or empty if the plan has none. */
    public Optional<PlannedCourse> findCourse(UUID id) {
        return courses.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    /** Returns the courses whose placement occupies {@code term}. A Y course is in both its Fall and Winter terms. */
    public List<PlannedCourse> coursesIn(AcademicTerm term) {
        return courses.stream().filter(c -> c.getPlacement().occupies(term)).collect(Collectors.toUnmodifiableList());
    }

    /** Returns the courses whose last term is before {@code term}. */
    public List<PlannedCourse> coursesEndedBefore(AcademicTerm term) {
        return courses.stream().filter(c -> c.getPlacement().endTerm().isBefore(term))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns the courses in the academic year that starts in Fall {@code academicYear}. A Summer term counts toward
     * the preceding academic year, so Summer 2027 counts toward 2026.
     */
    public List<PlannedCourse> coursesInAcademicYear(int academicYear) {
        return courses.stream().filter(c -> c.getPlacement().academicYear() == academicYear)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<String> enrolledPrograms() {
        return programCodesWith(ProgramStatus.ENROLLED);
    }

    public List<String> intendedPrograms() {
        return programCodesWith(ProgramStatus.INTENDED);
    }

    private List<String> programCodesWith(ProgramStatus status) {
        return programs.stream().filter(p -> p.getStatus() == status).map(ProgramSelection::getCode)
                .collect(Collectors.toUnmodifiableList());
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name;
    }

    /** Sets the degree type, or clears it if {@code degree} is {@code null}. */
    public void setDegree(DegreeType degree) {
        this.degree = degree;
    }

    public void addProgram(String code, ProgramStatus status) {
        if (programs.stream().anyMatch(p -> p.getCode().equals(code))) {
            throw new IllegalArgumentException(code + " is already in this plan");
        }
        programs.add(new ProgramSelection(code, status));
    }

    public void setProgramStatus(String code, ProgramStatus status) {
        int i = programIndex(code);
        programs.set(i, programs.get(i).withStatus(status));
    }

    public void removeProgram(String code) {
        if (!programs.removeIf(p -> p.getCode().equals(code))) {
            throw new IllegalArgumentException(code + " isn't in this plan");
        }
    }

    private int programIndex(String code) {
        for (int i = 0; i < programs.size(); i++) {
            if (programs.get(i).getCode().equals(code)) {
                return i;
            }
        }
        throw new IllegalArgumentException(code + " isn't in this plan");
    }

    public void addTerm(AcademicTerm term) {
        Objects.requireNonNull(term, "term");
        if (terms.contains(term)) {
            throw new IllegalArgumentException(term + " is already in this plan");
        }
        terms.add(term);
        Collections.sort(terms);
    }

    /**
     * @throws IllegalArgumentException if {@code term} isn't in the plan
     * @throws IllegalStateException if a course occupies {@code term}
     */
    public void removeTerm(AcademicTerm term) {
        if (!terms.contains(term)) {
            throw new IllegalArgumentException(term + " isn't in this plan");
        }
        if (!coursesIn(term).isEmpty()) {
            throw new IllegalStateException(term + " still has courses in it");
        }
        terms.remove(term);
    }

    /**
     * Adds a new entry made by {@link PlannedCourse#create} and returns it.
     *
     * @throws IllegalArgumentException if a term the placement occupies isn't in the plan, or the code is already in
     *     that placement
     */
    public PlannedCourse addCourse(String code, Placement placement) {
        PlannedCourse course = PlannedCourse.create(code, placement);
        insertCourse(course);
        return course;
    }

    /**
     * Replaces the entry that has the same id as {@code updated}.
     *
     * @throws IllegalArgumentException if no entry has that id, a term the placement occupies isn't in the plan, or
     *     another entry has the same code and placement
     */
    public void replaceCourse(PlannedCourse updated) {
        int i = courseIndex(updated.getId());
        checkFits(updated);
        courses.set(i, updated);
    }

    /**
     * Moves an entry to a new placement.
     *
     * @throws IllegalArgumentException if no entry has that id, a term the placement occupies isn't in the plan, or
     *     another entry has the same code and placement
     */
    public void moveCourse(UUID id, Placement to) {
        replaceCourse(courses.get(courseIndex(id)).withPlacement(to));
    }

    public void removeCourse(UUID id) {
        courses.remove(courseIndex(id));
    }

    private void insertCourse(PlannedCourse course) {
        if (findCourse(course.getId()).isPresent()) {
            throw new IllegalArgumentException("course id " + course.getId() + " is already in this plan");
        }
        checkFits(course);
        courses.add(course);
    }

    private void checkFits(PlannedCourse course) {
        for (AcademicTerm term : course.getPlacement().terms()) {
            if (!terms.contains(term)) {
                throw new IllegalArgumentException(term + " isn't in this plan");
            }
        }
        for (PlannedCourse other : courses) {
            if (!other.getId().equals(course.getId()) && other.getCode().equals(course.getCode())
                    && other.getPlacement().equals(course.getPlacement())) {
                throw new IllegalArgumentException(course.getCode() + " is already in " + course.getPlacement());
            }
        }
    }

    private int courseIndex(UUID id) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getId().equals(id)) {
                return i;
            }
        }
        throw new IllegalArgumentException("no course with id " + id);
    }

    public void addTransferCredit(TransferCredit credit) {
        Objects.requireNonNull(credit, "credit");
        if (transferCredits.stream().anyMatch(t -> t.getCode().equals(credit.getCode()))) {
            throw new IllegalArgumentException(credit.getCode() + " is already a transfer credit in this plan");
        }
        transferCredits.add(credit);
    }

    public void removeTransferCredit(String code) {
        if (!transferCredits.removeIf(t -> t.getCode().equals(code))) {
            throw new IllegalArgumentException(code + " isn't a transfer credit in this plan");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Plan)) {
            return false;
        }
        Plan that = (Plan) o;
        return id.equals(that.id) && name.equals(that.name) && degree == that.degree
                && programs.equals(that.programs) && terms.equals(that.terms) && courses.equals(that.courses)
                && transferCredits.equals(that.transferCredits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, degree, programs, terms, courses, transferCredits);
    }

    @Override
    public String toString() {
        return "Plan{" + name + ", " + id + "}";
    }
}
