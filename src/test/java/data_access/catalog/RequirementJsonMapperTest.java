package data_access.catalog;

import entity.requirement.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequirementJsonMapperTest {

    private final RequirementJsonMapper mapper = new RequirementJsonMapper();

    private Requirement parse(String json) {
        return mapper.parseNode(new JSONObject(json));
    }

    private static CourseFilter noFilter() {
        return CourseFilter.builder().build();
    }

    @Test
    void parsesAnd() {
        Requirement r = parse("{\"type\":\"and\",\"weight\":0.5,\"items\":[{\"type\":\"course\",\"code\":\"CSC110Y1\"}]}");
        assertEquals(new AndReq(List.of(new CourseReq("CSC110Y1", null, false)), 0.5, false), r);
    }

    @Test
    void parsesOr() {
        Requirement r = parse("{\"type\":\"or\",\"items\":["
                + "{\"type\":\"course\",\"code\":\"CSC148H1\",\"min_grade\":60},"
                + "{\"type\":\"course\",\"code\":\"CSC111H1\",\"min_grade\":60}]}");
        assertEquals(new OrReq(List.of(
                new CourseReq("CSC148H1", 60, false),
                new CourseReq("CSC111H1", 60, false)), false), r);
    }

    @Test
    void missingItemsGiveEmptyList() {
        assertEquals(new OrReq(List.of(), false), parse("{\"type\":\"or\"}"));
    }

    @Test
    void parsesNot() {
        Requirement r = parse("{\"type\":\"not\",\"items\":[{\"type\":\"program\",\"programs\":[\"ASMIN1017\"],"
                + "\"text\":\"Psychology Minor\"}]}");
        assertEquals(new NotReq(new ProgramReq(List.of("ASMIN1017"), "Psychology Minor", false), false), r);
    }

    @Test
    void parsesChoose() {
        Requirement r = parse("{\"type\":\"choose\",\"count\":2,\"items\":["
                + "{\"type\":\"course\",\"code\":\"PHY324H1\"},"
                + "{\"type\":\"course\",\"code\":\"PHY350H1\"},"
                + "{\"type\":\"course\",\"code\":\"AST325H1\"}]}");
        assertEquals(new ChooseReq(2, List.of(
                new CourseReq("PHY324H1", null, false),
                new CourseReq("PHY350H1", null, false),
                new CourseReq("AST325H1", null, false)), false), r);
    }

    @Test
    void parsesCourse() {
        Requirement r = parse("{\"type\":\"course\",\"code\":\"CSC148H1\",\"min_grade\":60}");
        assertEquals(new CourseReq("CSC148H1", 60, false), r);
    }

    @Test
    void parsesCourseWithoutMinGrade() {
        assertEquals(new CourseReq("CSC148H1", null, false), parse("{\"type\":\"course\",\"code\":\"CSC148H1\"}"));
    }

    @Test
    void parsesCredits() {
        Requirement r = parse("{\"type\":\"credits\",\"amount\":2,\"max_amount\":3,\"weight\":0.5,"
                + "\"lists\":[\"Requirement 1 courses\"]}");
        CourseFilter filter = CourseFilter.builder().lists(List.of("Requirement 1 courses")).build();
        assertEquals(new CreditsReq(2, 3.0, 0.5, filter, false), r);
    }

    @Test
    void creditsWithoutAmountDefaultsToZero() {
        assertEquals(new CreditsReq(0, null, null, noFilter(), false), parse("{\"type\":\"credits\"}"));
    }

    @Test
    void readsEveryFilterKey() {
        CreditsReq r = (CreditsReq) parse("{\"type\":\"credits\",\"amount\":1,"
                + "\"courses\":[\"CSC110Y1\"],\"designators\":[\"MAT\"],\"levels\":[300,400],\"breadth\":[5],"
                + "\"lists\":[\"Group A\"],\"requirements\":[2],\"exclude\":[\"MAT135H1\"],"
                + "\"exclude_designators\":[\"JSC\"],\"max_course_amount\":0.5,\"min_grade\":70,"
                + "\"transfer\":true,\"cr_ncr\":true}");
        CourseFilter expected = CourseFilter.builder()
                .courses(List.of("CSC110Y1"))
                .designators(List.of("MAT"))
                .levels(List.of(300, 400))
                .breadth(List.of(5))
                .lists(List.of("Group A"))
                .requirements(List.of(2))
                .exclude(List.of("MAT135H1"))
                .excludeDesignators(List.of("JSC"))
                .maxCourseAmount(0.5)
                .minGrade(70)
                .transfer(true)
                .crNcr(true)
                .build();
        assertEquals(expected, r.getFilter());
    }

    @Test
    void parsesAverage() {
        Requirement r = parse("{\"type\":\"average\",\"min_grade\":75,\"amount\":2,\"best\":true,"
                + "\"designators\":[\"CSC\"],\"items\":[{\"type\":\"credits\",\"amount\":1}]}");
        CourseFilter filter = CourseFilter.builder().designators(List.of("CSC")).build();
        assertEquals(new AverageReq(75, 2.0, true, filter,
                List.of(new CreditsReq(1, null, null, noFilter(), false)), false), r);
    }

    @Test
    void averageMinGradeIsNotAFilter() {
        AverageReq r = (AverageReq) parse("{\"type\":\"average\",\"min_grade\":75}");
        assertEquals(75, r.getMinGrade());
        assertTrue(r.getFilter().getMinGrade().isEmpty());
    }

    @Test
    void averageAcceptsOrAndChooseItems() {
        AverageReq r = (AverageReq) parse("{\"type\":\"average\",\"min_grade\":70,\"items\":["
                + "{\"type\":\"or\",\"items\":[{\"type\":\"credits\",\"amount\":1},{\"type\":\"credits\",\"amount\":2}]},"
                + "{\"type\":\"choose\",\"count\":1,\"items\":[{\"type\":\"credits\",\"amount\":1}]}]}");
        assertInstanceOf(OrReq.class, r.getItems().get(0));
        assertInstanceOf(ChooseReq.class, r.getItems().get(1));
    }

    @Test
    void parsesGpa() {
        Requirement r = parse("{\"type\":\"gpa\",\"min_gpa\":3.5,\"scope\":\"APM and MAT courses\"}");
        assertEquals(new GpaReq(3.5, "APM and MAT courses", false), r);
    }

    @Test
    void parsesGpaWithoutScope() {
        assertEquals(new GpaReq(2.5, null, false), parse("{\"type\":\"gpa\",\"min_gpa\":2.5}"));
    }

    @Test
    void parsesProgram() {
        Requirement r = parse("{\"type\":\"program\",\"programs\":[\"ASSPE1013\",\"ASMAJ1013\"],"
                + "\"text\":\"enrolment in Sociology program\"}");
        assertEquals(new ProgramReq(List.of("ASSPE1013", "ASMAJ1013"), "enrolment in Sociology program", false), r);
    }

    @Test
    void parsesRequirement() {
        assertEquals(new RowRefReq(List.of(5), false), parse("{\"type\":\"requirement\",\"requirements\":[5]}"));
    }

    @Test
    void parsesProfile() {
        Requirement r = parse("{\"type\":\"profile\",\"attr\":\"has_missing_numeric_grades\",\"value\":true,"
                + "\"text\":\"lacking numerical grades\"}");
        assertEquals(new ProfileReq("has_missing_numeric_grades", true, "lacking numerical grades", false), r);
    }

    @Test
    void profileNumberBecomesInteger() {
        JSONObject longValue = new JSONObject("{\"type\":\"profile\",\"attr\":\"min_year\",\"text\":\"year 2\"}")
                .put("value", 2L);
        JSONObject decimalValue = new JSONObject("{\"type\":\"profile\",\"attr\":\"min_year\",\"text\":\"year 2\"}")
                .put("value", new BigDecimal("2"));
        ProfileReq expected = new ProfileReq("min_year", 2, "year 2", false);
        assertEquals(expected, mapper.parseNode(longValue));
        assertEquals(expected, mapper.parseNode(decimalValue));
    }

    @Test
    void parsesText() {
        Requirement r = parse("{\"type\":\"text\",\"kind\":\"permission\",\"text\":\"permission of the department\"}");
        assertEquals(new TextReq(TextKind.PERMISSION, "permission of the department", false), r);
    }

    @Test
    void concurrentFlagIsRead() {
        assertTrue(parse("{\"type\":\"course\",\"code\":\"CSC207H1\",\"concurrent\":true}").isConcurrent());
        assertFalse(parse("{\"type\":\"course\",\"code\":\"CSC207H1\"}").isConcurrent());
    }

    @Test
    void unknownTypeThrows() {
        CatalogFormatException e = assertThrows(CatalogFormatException.class, () -> parse("{\"type\":\"bogus\"}"));
        assertEquals("unknown requirement node type: bogus", e.getMessage());
    }

    @Test
    void errorNamesTheNode() {
        CatalogFormatException e = assertThrows(CatalogFormatException.class,
                () -> parse("{\"type\":\"course\",\"min_grade\":60}"));
        assertTrue(e.getMessage().startsWith("bad node: "));
        assertTrue(e.getMessage().contains("\"min_grade\":60"));
        assertInstanceOf(JSONException.class, e.getCause());
    }

    @Test
    void invalidValueThrows() {
        CatalogFormatException e = assertThrows(CatalogFormatException.class,
                () -> parse("{\"type\":\"text\",\"kind\":\"bogus\",\"text\":\"x\"}"));
        assertInstanceOf(IllegalArgumentException.class, e.getCause());
    }

    @Test
    void notWithoutItemsThrows() {
        CatalogFormatException e = assertThrows(CatalogFormatException.class, () -> parse("{\"type\":\"not\"}"));
        assertInstanceOf(IndexOutOfBoundsException.class, e.getCause());
    }

    @Test
    void childErrorIsNotWrappedTwice() {
        CatalogFormatException e = assertThrows(CatalogFormatException.class,
                () -> parse("{\"type\":\"or\",\"items\":[{\"type\":\"bogus\"}]}"));
        assertEquals("unknown requirement node type: bogus", e.getMessage());
        assertNull(e.getCause());
    }
}
