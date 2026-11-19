package entity.evaluation;

import entity.term.AcademicTerm;

import java.util.*;

public final class AcademicSnapshot {
    private final Map<String, CourseRecord> taken;
    private final Map<String, CourseRecord> concurrent;
    private final Set<String> enrolledPrograms;
    private final Set<String> intendedPrograms;
    private final Double cgpa;
    private final Map<String, Object> profileFacts;
    private final AcademicTerm asOf;

    private AcademicSnapshot(Map<String, CourseRecord> taken, Map<String, CourseRecord> concurrent,
                             Set<String> enrolledPrograms, Set<String> intendedPrograms, Double cgpa,
                             Map<String, Object> profileFacts, AcademicTerm asOf) {
        this.taken = taken;
        this.concurrent = concurrent;
        this.enrolledPrograms = enrolledPrograms;
        this.intendedPrograms = intendedPrograms;
        this.cgpa = cgpa;
        this.profileFacts = profileFacts;
        this.asOf = asOf;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, CourseRecord> getTaken() {
        return taken;
    }

    public Map<String, CourseRecord> getConcurrent() {
        return concurrent;
    }

    public Set<String> getEnrolledPrograms() {
        return enrolledPrograms;
    }

    public Set<String> getIntendedPrograms() {
        return intendedPrograms;
    }

    public OptionalDouble getCgpa() {
        return cgpa == null ? OptionalDouble.empty() : OptionalDouble.of(cgpa);
    }

    public Optional<Object> profileFact(String attr) {
        return Optional.ofNullable(profileFacts.get(attr));
    }

    public Optional<AcademicTerm> getAsOf() {
        return Optional.ofNullable(asOf);
    }

    public Collection<CourseRecord> allRecords() {
        Collection<CourseRecord> all = new ArrayList<>(taken.values());
        all.addAll(concurrent.values());

        return all;
    }

    public AcademicSnapshot restrictedTo(Set<String> codes) {
        Map<String, CourseRecord> takenCopy = new LinkedHashMap<>(taken);
        takenCopy.keySet().retainAll(codes);

        Map<String, CourseRecord> concurrentCopy = new LinkedHashMap<>(concurrent);
        concurrentCopy.keySet().retainAll(codes);

        return new AcademicSnapshot(Collections.unmodifiableMap(takenCopy), Collections.unmodifiableMap(concurrentCopy),
                enrolledPrograms, intendedPrograms, cgpa, profileFacts, asOf);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AcademicSnapshot)) {
            return false;
        }
        AcademicSnapshot that = (AcademicSnapshot) o;
        return taken.equals(that.taken)
                && concurrent.equals(that.concurrent)
                && enrolledPrograms.equals(that.enrolledPrograms)
                && intendedPrograms.equals(that.intendedPrograms)
                && Objects.equals(cgpa, that.cgpa)
                && profileFacts.equals(that.profileFacts)
                && Objects.equals(asOf, that.asOf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taken, concurrent, enrolledPrograms, intendedPrograms, cgpa, profileFacts, asOf);
    }

    @Override
    public String toString() {
        return "AcademicSnapshot{taken=" + taken.values() + ", concurrent=" + concurrent.values() + ", enrolled="
                + enrolledPrograms + ", intended=" + intendedPrograms + ", cgpa=" + cgpa + ", facts=" + profileFacts
                + ", asOf=" + asOf + "}";
    }

    public static final class Builder {
        private final Map<String, CourseRecord> taken = new LinkedHashMap<>();
        private final Map<String, CourseRecord> concurrent = new LinkedHashMap<>();
        private final Set<String> enrolledPrograms = new LinkedHashSet<>();
        private final Set<String> intendedPrograms = new LinkedHashSet<>();
        private final Map<String, Object> profileFacts = new LinkedHashMap<>();
        private Double cgpa;
        private AcademicTerm asOf;

        public Builder taken(Collection<CourseRecord> records) {
            for (CourseRecord r : records) {
                taken.put(r.getCode(), r);
            }

            return this;
        }

        public Builder taken(CourseRecord... records) {
            return taken(Arrays.asList(records));
        }

        public Builder concurrent(Collection<CourseRecord> records) {
            for (CourseRecord r : records) {
                concurrent.put(r.getCode(), r);
            }

            return this;
        }

        public Builder concurrent(CourseRecord... records) {
            return concurrent(Arrays.asList(records));
        }

        public Builder enrolledProgram(String code) {
            enrolledPrograms.add(code);

            return this;
        }

        public Builder intendedProgram(String code) {
            intendedPrograms.add(code);

            return this;
        }

        public Builder cgpa(Double cgpa) {
            this.cgpa = cgpa;

            return this;
        }

        public Builder profileFact(String attr, Object value) {
            profileFacts.put(attr, value);

            return this;
        }

        public Builder asOf(AcademicTerm term) {
            this.asOf = term;

            return this;
        }

        public AcademicSnapshot build() {
            for (String code : taken.keySet()) {
                if (concurrent.containsKey(code)) {
                    throw new IllegalArgumentException("in both taken and concurrent: " + code);
                }
            }

            return new AcademicSnapshot(
                    Collections.unmodifiableMap(new LinkedHashMap<>(taken)),
                    Collections.unmodifiableMap(new LinkedHashMap<>(concurrent)),
                    Collections.unmodifiableSet(new LinkedHashSet<>(enrolledPrograms)),
                    Collections.unmodifiableSet(new LinkedHashSet<>(intendedPrograms)),
                    cgpa,
                    Collections.unmodifiableMap(new LinkedHashMap<>(profileFacts)),
                    asOf);
        }
    }
}
