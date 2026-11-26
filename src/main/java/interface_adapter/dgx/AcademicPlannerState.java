package interface_adapter.dgx;

import java.util.List;
import java.util.Objects;

/** Display data for the academic planner. It contains no domain entities. */
public final class AcademicPlannerState {
    private final List<TermRow> terms;
    private final String message;

    public AcademicPlannerState() {
        this(List.of(), "");
    }

    public AcademicPlannerState(List<TermRow> terms, String message) {
        this.terms = List.copyOf(Objects.requireNonNull(terms, "terms"));
        this.message = Objects.requireNonNull(message, "message");
    }

    public List<TermRow> getTerms() {
        return terms;
    }

    public String getMessage() {
        return message;
    }

    /** One visible academic-term destination in the planner. */
    public static final class TermRow {
        private final int year;
        private final String season;
        private final List<CourseRow> courses;

        public TermRow(int year, String season, List<CourseRow> courses) {
            this.year = year;
            this.season = Objects.requireNonNull(season, "season");
            this.courses = List.copyOf(Objects.requireNonNull(courses, "courses"));
        }

        public int getYear() {
            return year;
        }

        public String getSeason() {
            return season;
        }

        public List<CourseRow> getCourses() {
            return courses;
        }

        public String getLabel() {
            return season + " " + year;
        }
    }

    /** The planner only needs the course code to render a planned-course row. */
    public static final class CourseRow {
        private final String code;

        public CourseRow(String code) {
            this.code = Objects.requireNonNull(code, "code");
        }

        public String getCode() {
            return code;
        }
    }
}
