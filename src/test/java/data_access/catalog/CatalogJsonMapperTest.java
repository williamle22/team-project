package data_access.catalog;

import entity.catalog.*;
import entity.requirement.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import testutil.CatalogFixtures;
import testutil.JsonFixtures;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogJsonMapperTest {

    private final CatalogJsonMapper mapper = new CatalogJsonMapper();

    private static String column(String fixture, String key) {
        return new JSONObject(JsonFixtures.read(fixture)).get(key).toString();
    }

    @Test
    void phy152PrerequisiteIsOrOfTwoCourses() {
        Requisite r = mapper.parseRequisite(column("PHY152H1.json", "prerequisites"));
        assertEquals(new OrReq(List.of(
                new CourseReq("PHY131H1", null, false),
                new CourseReq("PHY151H1", null, false)), false), r.getReq().orElseThrow());
        assertEquals(List.of(), r.getNotes());
        assertEquals("PHY131H1/  PHY151H1", r.getRaw());
    }

    @Test
    void phy152CorequisiteNote() {
        Requisite r = mapper.parseRequisite(column("PHY152H1.json", "corequisites"));
        assertEquals(List.of("MAT149H1 recommended"), r.getNotes());
        assertEquals(5, ((OrReq) r.getReq().orElseThrow()).getItems().size());
    }

    @Test
    void phy152SessionsWinterOnly() {
        Sessions s = mapper.parseSessions(column("PHY152H1.json", "sessions"));
        assertTrue(s.offeredInWinter());
        assertFalse(s.offeredInFall());
        assertFalse(s.offeredInSummer());
    }

    @Test
    void phy152ExclusionIsCourseSet() {
        Exclusions e = mapper.parseExclusions(column("PHY152H1.json", "exclusions"));
        assertEquals(1, e.getItems().size());
        assertTrue(e.getItems().get(0).isCourseSet());
        assertEquals(List.of("PHY132H1"), e.getItems().get(0).getCourses());
    }

    @Test
    void phy152BreadthIsCategoryFive() {
        assertEquals(List.of(BreadthCategory.PHYSICAL_MATHEMATICAL),
                mapper.parseBreadth(column("PHY152H1.json", "breadth")));
    }

    @Test
    void drm228PrerequisiteHasProgramNode() {
        AndReq and = (AndReq) mapper.parseRequisite(column("DRM228H1.json", "prerequisites")).getReq().orElseThrow();
        assertEquals(new ProgramReq(List.of("ASMAJ2148", "ASSPE2148", "ASMIN2148"), "Drama Major, Specialist or Minor",
                false), and.getItems().get(0));
        assertEquals(new CourseReq("DRM101Y1", null, false), and.getItems().get(1));
    }

    @Test
    void asmin0667EnrolmentIsOpen() {
        Enrolment e = mapper.parseEnrolment(column("ASMIN0667.json", "enrolment"));
        assertEquals(EnrolmentKind.OPEN, e.getKind());
        assertEquals(new CreditsReq(4, null, null, CourseFilter.builder().build(), false), e.getReq().orElseThrow());
        assertEquals(List.of(), e.getExclusions());
        assertFalse(e.isFirstRoundOnly());
    }

    @Test
    void asmin0667AreasAreRead() {
        assertEquals(List.of("History and Philosophy of Science and Technology"),
                mapper.parseAreas(column("ASMIN0667.json", "areas")));
    }

    @Test
    void asmin0667RowTwoIsLimit() {
        Completion c = mapper.parseCompletion(column("ASMIN0667.json", "completion"));
        assertEquals(3, c.getRequirements().size());
        assertEquals(2, c.getNotes().size());

        CompletionRow row = c.getRequirements().get(1);
        assertEquals(2, row.getNumber());
        assertEquals(RowKind.LIMIT, row.getKind());
        assertEquals(1, row.getParent().orElseThrow());
        CreditsReq req = (CreditsReq) row.getReq().orElseThrow();
        assertEquals(1.0, req.getMaxAmount().orElseThrow());
        assertEquals(List.of("HPS"), req.getFilter().getDesignators());
        assertEquals(List.of(100), req.getFilter().getLevels());
    }

    @Test
    void asmin0667TopRowHasNoParent() {
        CompletionRow row = mapper.parseCompletion(column("ASMIN0667.json", "completion")).getRequirements().get(0);
        assertTrue(row.isTopLevel());
        assertTrue(row.getLabel().isEmpty());
        assertFalse(row.isTranscriptScope());
        assertTrue(row.getAudience().isEmpty());
    }

    @Test
    void asmin1001NestedList() {
        Completion c = mapper.parseCompletion(column("ASMIN1001.json", "completion"));
        CourseList list = c.getLists().stream()
                .filter(l -> l.getName().equals("Requirement 4 courses"))
                .findFirst().orElseThrow();
        assertTrue(list.getTitle().isEmpty());
        CreditsReq second = (CreditsReq) list.getItems().get(1);
        assertEquals(List.of("Group 2"), second.getFilter().getLists());
    }

    @Test
    void asmin1001ListsKeepOrderAndTitles() {
        List<CourseList> lists = mapper.parseCompletion(column("ASMIN1001.json", "completion")).getLists();
        assertEquals(3, lists.size());
        assertEquals("Group 1", lists.get(0).getName());
        assertEquals("Group 1 - Advanced Bioethics", lists.get(0).getTitle().orElseThrow());
    }

    @Test
    void asmin1001EqualsHandBuiltTree() {
        assertEquals(CatalogFixtures.asmin1001(), mapper.parseCompletion(column("ASMIN1001.json", "completion")));
    }

    @Test
    void nullColumnsGiveNullOrEmpty() {
        assertNull(mapper.parseRequisite(null));
        assertNull(mapper.parseExclusions(null));
        assertEquals(List.of(), mapper.parseBreadth(null));
        assertEquals(List.of(), mapper.parseAreas(null));
        assertNull(mapper.parseSessions(null));
        assertNull(mapper.parseEnrolment(null));
        assertNull(mapper.parseCompletion(null));
    }

    @Test
    void requisiteWithNullReq() {
        Requisite r = mapper.parseRequisite("{\"req\":null,\"raw\":\"see department\"}");
        assertTrue(r.getReq().isEmpty());
        assertEquals("see department", r.getRaw());
    }

    @Test
    void exclusionsReadFilterAndTextItems() {
        Exclusions e = mapper.parseExclusions("{\"items\":["
                + "{\"designators\":[\"CSC\"],\"levels\":[100],\"exclude\":[\"CSC196H1\"],\"concurrent\":true,"
                + "\"note\":\"except transfer credits\"},"
                + "{\"kind\":\"placement\",\"text\":\"Native Korean speakers\"}],"
                + "\"notes\":[\"a note\"],\"raw\":\"CSC; Native Korean speakers\"}");
        ExclusionItem filter = e.getItems().get(0);
        assertEquals(new ExclusionItem(List.of(), List.of("CSC"), List.of(100), List.of("CSC196H1"), true,
                "except transfer credits", null, null), filter);
        assertTrue(filter.isFilter());

        ExclusionItem text = e.getItems().get(1);
        assertEquals(TextKind.PLACEMENT, text.getKind().orElseThrow());
        assertEquals("Native Korean speakers", text.getText().orElseThrow());
        assertTrue(text.isText());

        assertEquals(List.of("a note"), e.getNotes());
    }

    @Test
    void sessionsReadSummer() {
        Sessions s = mapper.parseSessions("{\"fall_winter\":[\"F\",\"Y\"],\"summer\":[\"S\"]}");
        assertEquals(List.of(Section.F, Section.Y), s.getFallWinter());
        assertEquals(List.of(Section.S), s.getSummer());
    }

    @Test
    void suspendedEnrolmentWithExclusions() {
        Enrolment e = mapper.parseEnrolment("{\"kind\":\"suspended\",\"req\":null,\"first_round_only\":true,"
                + "\"exclusions\":[{\"type\":\"program\",\"programs\":[\"ASMAJ1013\"],\"text\":\"Sociology Major\"}],"
                + "\"notes\":[\"n\"],\"raw\":\"suspended\"}");
        assertEquals(EnrolmentKind.SUSPENDED, e.getKind());
        assertTrue(e.getReq().isEmpty());
        assertEquals(List.of(new ProgramReq(List.of("ASMAJ1013"), "Sociology Major", false)), e.getExclusions());
        assertTrue(e.isFirstRoundOnly());
        assertEquals(List.of("n"), e.getNotes());
    }

    @Test
    void enrolmentExclusionMustBeProgram() {
        CatalogFormatException e = assertThrows(CatalogFormatException.class, () -> mapper.parseEnrolment(
                "{\"kind\":\"open\",\"req\":{\"type\":\"credits\",\"amount\":4},"
                        + "\"exclusions\":[{\"type\":\"course\",\"code\":\"CSC110Y1\"}],\"raw\":\"x\"}"));
        assertTrue(e.getMessage().startsWith("enrolment exclusion must be a program"));
    }

    @Test
    void completionRowReadsEveryField() {
        Completion c = mapper.parseCompletion("{\"requirements\":[{\"number\":5,\"kind\":\"limit\",\"label\":\"1\","
                + "\"section\":\"Higher Years\",\"parent\":1,\"overlaps\":[13],\"req\":{\"type\":\"credits\",\"max_amount\":0},"
                + "\"audience\":{\"type\":\"profile\",\"attr\":\"faculty\",\"value\":\"Arts & Science\",\"text\":\"FAS students\"},"
                + "\"scope\":\"transcript\",\"notes\":[\"n\"],\"raw\":\"row raw\"}],"
                + "\"exclusions\":[{\"type\":\"program\",\"programs\":[\"ASMAJ0652\"],\"text\":\"Major\"},"
                + "{\"type\":\"or\",\"items\":[]}],\"raw\":\"completion raw\"}");
        CompletionRow row = c.getRequirements().get(0);
        assertEquals(new CompletionRow(5, RowKind.LIMIT, "1", "Higher Years", 1, List.of(13),
                new CreditsReq(0, 0.0, null, CourseFilter.builder().build(), false),
                new ProfileReq("faculty", "Arts & Science", "FAS students", false),
                true, List.of("n"), "row raw"), row);
        assertEquals(List.of(), c.getLists());
        assertEquals(2, c.getExclusions().size());
        assertInstanceOf(OrReq.class, c.getExclusions().get(1));
    }

    @Test
    void optionGroupRowHasNullReq() {
        CompletionRow row = mapper.parseCompletion("{\"requirements\":[{\"number\":1,\"kind\":\"allocation\","
                + "\"req\":null,\"raw\":\"r\"}],\"raw\":\"c\"}").getRequirements().get(0);
        assertTrue(row.isOptionGroup());
        assertTrue(row.getReq().isEmpty());
    }

    @Test
    void audienceMustBeProfile() {
        CatalogFormatException e = assertThrows(CatalogFormatException.class, () -> mapper.parseCompletion(
                "{\"requirements\":[{\"number\":1,\"kind\":\"allocation\",\"req\":null,"
                        + "\"audience\":{\"type\":\"course\",\"code\":\"CSC110Y1\"},\"raw\":\"r\"}],\"raw\":\"c\"}"));
        assertTrue(e.getMessage().startsWith("row audience must be a profile"));
    }

    @Test
    void badColumnNamesTheColumn() {
        assertBad("bad requisite: ", () -> mapper.parseRequisite("{\"req\":null}"));
        assertBad("bad exclusions: ", () -> mapper.parseExclusions("{\"raw\":\"x\"}"));
        assertBad("bad breadths: ", () -> mapper.parseBreadth("[6]"));
        assertBad("bad areas: ", () -> mapper.parseAreas("{}"));
        assertBad("bad sessions: ", () -> mapper.parseSessions("{\"fall_winter\":[\"X\"]}"));
        assertBad("bad enrolment: ", () -> mapper.parseEnrolment("{\"kind\":\"open\",\"req\":null,\"raw\":\"x\"}"));
        assertBad("bad completion: ", () -> mapper.parseCompletion("{\"raw\":\"x\"}"));
        assertBad("bad completion: ", () -> mapper.parseCompletion(
                "{\"requirements\":[{\"number\":0,\"kind\":\"allocation\",\"req\":null,\"raw\":\"r\"}],\"raw\":\"c\"}"));
    }

    @Test
    void badRequirementNodeIsNotWrappedAgain() {
        CatalogFormatException e = assertThrows(CatalogFormatException.class,
                () -> mapper.parseRequisite("{\"req\":{\"type\":\"bogus\"},\"raw\":\"x\"}"));
        assertEquals("unknown requirement node type: bogus", e.getMessage());
    }

    private static void assertBad(String prefix, Executable call) {
        CatalogFormatException e = assertThrows(CatalogFormatException.class, call);
        assertTrue(e.getMessage().startsWith(prefix), e.getMessage());
        assertNotNull(e.getCause());
    }
}
