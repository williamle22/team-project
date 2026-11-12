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

    public static Plan create(String name) {
        return new Plan(UUID.randomUUID(), name);
    }

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

    public Plan copy() {
        return restore(id, name, degree, programs, terms, courses, transferCredits);
    }

    public Plan copyWithNewId() {
        return restore(UUID.randomUUID(), name, degree, programs, terms, courses, transferCredits);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

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

    public Optional<PlannedCourse> findCourse(UUID id) {
        return courses.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public List<PlannedCourse> coursesIn(AcademicTerm term) {
        return courses.stream().filter(c -> c.getPlacement().occupies(term)).collect(Collectors.toUnmodifiableList());
    }

    public List<PlannedCourse> coursesEndedBefore(AcademicTerm term) {
        return courses.stream().filter(c -> c.getPlacement().endTerm().isBefore(term))
                .collect(Collectors.toUnmodifiableList());
    }

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

    public void removeTerm(AcademicTerm term) {
        if (!terms.contains(term)) {
            throw new IllegalArgumentException(term + " isn't in this plan");
        }
        if (!coursesIn(term).isEmpty()) {
            throw new IllegalStateException(term + " still has courses in it");
        }
        terms.remove(term);
    }

    public PlannedCourse addCourse(String code, Placement placement) {
        PlannedCourse course = PlannedCourse.create(code, placement);
        insertCourse(course);
        return course;
    }

    public void replaceCourse(PlannedCourse updated) {
        int i = courseIndex(updated.getId());
        checkFits(updated);
        courses.set(i, updated);
    }

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
