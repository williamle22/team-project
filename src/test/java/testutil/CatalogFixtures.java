package testutil;

import entity.catalog.BreadthCategory;
import entity.catalog.Campus;
import entity.catalog.Completion;
import entity.catalog.Course;
import entity.catalog.ExclusionItem;
import entity.catalog.Exclusions;
import entity.catalog.Program;
import entity.catalog.ProgramType;
import entity.catalog.Requisite;
import entity.catalog.Section;
import entity.catalog.Sessions;
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
}
