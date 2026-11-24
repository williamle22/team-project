package testutil;

import entity.catalog.BreadthCategory;
import entity.catalog.Campus;
import entity.catalog.Completion;
import entity.catalog.CompletionRow;
import entity.catalog.Course;
import entity.catalog.CourseList;
import entity.catalog.ExclusionItem;
import entity.catalog.Exclusions;
import entity.catalog.Program;
import entity.catalog.ProgramType;
import entity.catalog.Requisite;
import entity.catalog.RowKind;
import entity.catalog.Section;
import entity.catalog.Sessions;
import entity.degree.Degree;
import entity.degree.DegreeType;
import entity.degree.ProgramCombinationRule;
import entity.evaluation.CourseRecord;
import entity.requirement.AndReq;
import entity.requirement.ChooseReq;
import entity.requirement.CourseFilter;
import entity.requirement.CourseReq;
import entity.requirement.CreditsReq;
import entity.requirement.NotReq;
import entity.requirement.OrReq;
import entity.requirement.ProgramReq;
import entity.requirement.Requirement;
import entity.requirement.TextKind;
import entity.requirement.TextReq;

import java.util.List;
import java.util.Map;

/**
 * One-line builders for catalog test data. Each course's designator, number, level, and weight come from its code,
 * so use real-format codes such as {@code CSC148H1} or {@code MATA31H3}.
 */
public final class CatalogFixtures {
    private CatalogFixtures() {
    }

    public static Course course(String code) {
        return course(code, null, null, null, List.of(), null);
    }

    public static Course course(String code, Requisite prereqs, Requisite coreqs, Exclusions exclusions,
                                List<BreadthCategory> breadth, Sessions sessions) {
        char levelChar = code.charAt(3);
        int level = Character.isDigit(levelChar) ? (levelChar - '0') * 100 : (levelChar - 'A' + 1) * 100;
        double weight = code.charAt(6) == 'Y' ? 1.0 : 0.5;
        return new Course(code, code.substring(0, 3), code.substring(3, 6), level, weight, Campus.UTSG,
                code + " title", "", prereqs, coreqs, exclusions, breadth, sessions);
    }

    public static Course withPrereq(String code, Requirement req) {
        return course(code, requisite(req), null, null, List.of(), null);
    }

    public static Course withCoreq(String code, Requirement req) {
        return course(code, null, requisite(req), null, List.of(), null);
    }

    public static Course withExclusions(String code, ExclusionItem... items) {
        return course(code, null, null, new Exclusions(List.of(items), List.of(), ""), List.of(), null);
    }

    public static Course withBreadth(String code, BreadthCategory... breadth) {
        return course(code, null, null, null, List.of(breadth), null);
    }

    public static Course withSessions(String code, List<Section> fallWinter, List<Section> summer) {
        return course(code, null, null, null, List.of(), new Sessions(fallWinter, summer));
    }

    public static Requisite requisite(Requirement req) {
        return new Requisite(req, List.of(), "");
    }

    public static CourseReq req(String code) {
        return new CourseReq(code, null, false);
    }

    public static CourseReq req(String code, int minGrade) {
        return new CourseReq(code, minGrade, false);
    }

    public static CourseReq concurrentReq(String code) {
        return new CourseReq(code, null, true);
    }

    public static AndReq and(Requirement... items) {
        return new AndReq(List.of(items), null, false);
    }

    public static OrReq or(Requirement... items) {
        return new OrReq(List.of(items), false);
    }

    public static ChooseReq choose(int count, Requirement... items) {
        return new ChooseReq(count, List.of(items), false);
    }

    public static NotReq not(Requirement item) {
        return new NotReq(item, false);
    }

    public static CreditsReq credits(double amount, CourseFilter filter) {
        return new CreditsReq(amount, null, null, filter, false);
    }

    public static CreditsReq creditsIn(double amount, String... designators) {
        return credits(amount, CourseFilter.builder().designators(List.of(designators)).build());
    }

    public static TextReq text(String text) {
        return new TextReq(TextKind.OTHER, text, false);
    }

    public static ProgramReq programReq(String... codes) {
        return new ProgramReq(List.of(codes), String.join(" or ", codes), false);
    }

    public static ExclusionItem excludesCourses(String... codes) {
        return new ExclusionItem(List.of(codes), List.of(), List.of(), List.of(), false, null, null, null);
    }

    public static Program program(String code, ProgramType type, Completion completion) {
        return new Program(code, type, code.substring(5), code + " title", null, completion, List.of());
    }

    public static CourseRecord record(String code) {
        return new CourseRecord(course(code), null, false, false);
    }

    public static CourseRecord record(String code, int grade) {
        return record(course(code), grade);
    }

    public static CourseRecord record(Course course, int grade) {
        return new CourseRecord(course, grade, false, false);
    }

    public static CreditsReq fromCourses(String... codes) {
        return credits(0, CourseFilter.builder().courses(List.of(codes)).build());
    }

    public static CreditsReq fromLists(double amount, String... lists) {
        return credits(amount, CourseFilter.builder().lists(List.of(lists)).build());
    }

    public static CompletionRow completionRow(int number, RowKind kind, String label, Integer parent,
                                              Requirement req, String raw) {
        return new CompletionRow(number, kind, label, null, parent, List.of(), req, null, false, List.of(), raw);
    }

    public static Completion completion(List<CompletionRow> rows, CourseList... lists) {
        return new Completion(rows, List.of(lists), List.of(), List.of(), "raw");
    }

    /** ASMIN1001 (Bioethics Minor), copied by hand from {@code data/dgx.db}. */
    public static Completion asmin1001() {
        List<CompletionRow> rows = List.of(
                completionRow(1, RowKind.ALLOCATION, null, null, creditsIn(4, "PHL"),
                        "(4.0 credits including at least 1.0 credit at the 300+ level)\n"
                                + "5. Additional philosophy courses to a total of 4.0 credits"),
                completionRow(2, RowKind.CONSTRAINT, null, 1,
                        credits(1, CourseFilter.builder().levels(List.of(300, 400)).build()),
                        "including at least 1.0 credit at the 300+ level"),
                completionRow(3, RowKind.ALLOCATION, "1", 1, req("PHL281H1"), "1. PHL281H1"),
                completionRow(4, RowKind.ALLOCATION, "2", 1, or(req("PHL245H1"), req("PHL246H1")),
                        "2. PHL245H1/ PHL246H1"),
                completionRow(5, RowKind.ALLOCATION, "3", 1, fromLists(0.5, "Group 1"),
                        "3. 0.5 credit from Group 1 (Advanced Bioethics)"),
                completionRow(6, RowKind.ALLOCATION, "4", 1, fromLists(1, "Requirement 4 courses"),
                        "4. 1.0 credit from PHL271H1 / PHL275H1 / Group 2 (Value Theory)"));
        List<CourseList> lists = List.of(
                new CourseList("Group 1", "Group 1 - Advanced Bioethics", List.of(fromCourses(
                        "PHL380H1", "PHL381H1", "PHL382H1", "PHL383H1", "PHL384H1", "PHL440H1", "PHL470H1"))),
                new CourseList("Group 2", "Group 2 - Value Theory", List.of(fromCourses(
                        "PHL265H1", "PHL295H1", "PHL365H1", "PHL366H1", "PHL370H1", "PHL375H1", "PHL407H1",
                        "PHL412H1", "PHL413H1", "PHL483H1"))),
                new CourseList("Requirement 4 courses", null,
                        List.of(fromCourses("PHL271H1", "PHL275H1"), fromLists(0, "Group 2"))));
        String raw = "(4.0 credits including at least 1.0 credit at the 300+ level)\n1. PHL281H1\n"
                + "2. PHL245H1/ PHL246H1\n3. 0.5 credit from Group 1 (Advanced Bioethics)\n"
                + "4. 1.0 credit from PHL271H1 / PHL275H1 / Group 2 (Value Theory)\n"
                + "5. Additional philosophy courses to a total of 4.0 credits\n"
                + "_Group 1 - Advanced Bioethics:_\n"
                + "PHL380H1, PHL381H1, PHL382H1, PHL383H1, PHL384H1, PHL440H1, PHL470H1\n"
                + "_Group 2 - Value Theory:_\n"
                + "PHL265H1, PHL295H1, PHL365H1, PHL366H1, PHL370H1, PHL375H1, PHL407H1, PHL412H1, PHL413H1, "
                + "PHL483H1";

        return new Completion(rows, lists, List.of(), List.of(), raw);
    }

    /** ASMIN0667 (History and Philosophy of Science and Technology Minor), copied by hand from {@code data/dgx.db}. */
    public static Completion asmin0667() {
        CourseFilter hps = CourseFilter.builder().designators(List.of("HPS"))
                .courses(List.of("JHE353H1", "JHE355H1", "JPH311H1", "PHL245H1", "PHL356H1", "PHL357H1")).build();
        List<CompletionRow> rows = List.of(
                completionRow(1, RowKind.ALLOCATION, null, null, credits(4, hps),
                        "(4.0 credits)\n3. Additional HPS courses to a total of 4.0 credits, with at least 1.0 credit "
                                + "at the 300+level. JHE353H1, JHE355H1, JPH311H1, PHL245H1, PHL356H1, or PHL357H1 "
                                + "may also be used to fulfill this requirement."),
                completionRow(2, RowKind.LIMIT, null, 1, new CreditsReq(0, 1.0, null,
                                CourseFilter.builder().designators(List.of("HPS")).levels(List.of(100)).build(), false),
                        "Up to 1.0 credit in HPS courses at the 100-level will be counted towards the minor"),
                completionRow(3, RowKind.CONSTRAINT, null, 1,
                        credits(1, CourseFilter.builder().levels(List.of(300, 400)).build()),
                        "with at least 1.0 credit at the 300+level"));
        List<String> notes = List.of("Recommended: 0.5 credit from any HPS 100-level courses",
                "Recommended: at least 0.5 credit from any HPS 200-level courses");
        String raw = "(4.0 credits)\n1. Recommended: 0.5 credit from any HPS 100-level courses. Up to 1.0 credit in "
                + "HPS courses at the 100-level will be counted towards the minor.\n2. Recommended: at least 0.5 "
                + "credit from any HPS 200-level courses.\n3. Additional HPS courses to a total of 4.0 credits, with "
                + "at least 1.0 credit at the 300+level. JHE353H1, JHE355H1, JPH311H1, PHL245H1, PHL356H1, or "
                + "PHL357H1 may also be used to fulfill this requirement.";

        return new Completion(rows, List.of(), List.of(), notes, raw);
    }

    /** A small HBSC: 3.0 credits, at least 1.0 at the 300+ level, at most 1.0 at the 100 level, and 2 Majors. */
    public static Degree testDegree() {
        List<CompletionRow> rows = List.of(
                completionRow(1, RowKind.ALLOCATION, null, null, credits(3, CourseFilter.any()), "3.0 credits"),
                completionRow(2, RowKind.CONSTRAINT, null, 1,
                        credits(1, CourseFilter.builder().levels(List.of(300, 400)).build()),
                        "at least 1.0 credit at the 300+ level"),
                completionRow(3, RowKind.LIMIT, null, 1,
                        new CreditsReq(0, 1.0, null, CourseFilter.builder().levels(List.of(100)).build(), false),
                        "at most 1.0 credit at the 100 level"));

        return new Degree(DegreeType.HBSC, completion(rows),
                new ProgramCombinationRule(List.of(Map.of(ProgramType.MAJOR, 2))));
    }
}
