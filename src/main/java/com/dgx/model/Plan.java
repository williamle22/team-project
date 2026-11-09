package com.dgx.model;

import lombok.Getter;
import lombok.Setter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDate;

import com.dgx.exception.CourseEntryNotFoundException;

@Getter
public class Plan {
    private static int counter = 0;
    private static final int DEFAULT_YEARS_GENERATED = 5;

    // Plan Metadata
    private int id;

    @Setter
    private String title;
    @Setter
    private String description;

    // Plan information
    private Set<Program> programs;
    // TODO: might need ordered set
    private HashMap<CourseEntry, Set<Course>> courses;

    Plan(String title, String description) {
        this.title = title;
        this.description = description;
        this.programs = new HashSet<>();
        this.courses = new HashMap<>();

        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < DEFAULT_YEARS_GENERATED; i++) {
            courses.put(new CourseEntry(Term.FALL, currentYear + i), new HashSet<>());
            courses.put(new CourseEntry(Term.WINTER, currentYear + i), new HashSet<>());
            courses.put(new CourseEntry(Term.SUMMER, currentYear + i), new HashSet<>());
        }
    }

    Plan(String title) {
        this(title, "");
    }

    public void AddProgram(Program program) {
        programs.add(program);
    }

    public void RemoveProgram(Program program) {
        programs.remove(program);
    }

    public void AddCourseTo(CourseEntry courseEntry, Course course) {
        // Fatal error: courseEntry not in courses map
        if (!courses.containsKey(courseEntry)) {
            // TODO: maybe make this message slightly nicer
            throw new CourseEntryNotFoundException("Term=" + courseEntry.term + ", year=" + courseEntry.year + " not found.");
        }

        courses.get(courseEntry).add(course);
    }

    public static class CourseEntry {
        private final Term term;
        private final int year;

        CourseEntry(Term term, int year) {
            this.term = term;
            this.year = year;
        }
    }
}