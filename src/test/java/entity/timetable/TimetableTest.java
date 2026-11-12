package entity.timetable;

import entity.catalog.Section;
import entity.term.AcademicTerm;
import entity.term.Placement;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static entity.term.AcademicTerm.fall;
import static entity.term.AcademicTerm.winter;
import static entity.timetable.ActivityType.LEC;
import static entity.timetable.ActivityType.PRA;
import static entity.timetable.ActivityType.TUT;
import static org.junit.jupiter.api.Assertions.*;

class TimetableTest {
    private static final MeetingTime ENG140_FALL = meeting(fall(2026), DayOfWeek.FRIDAY, 13, 15, "BT");
    private static final MeetingTime ENG140_WINTER = meeting(winter(2027), DayOfWeek.FRIDAY, 13, 15, "AH");
    private static final MeetingTime CSC108_WINTER = meeting(winter(2027), DayOfWeek.FRIDAY, 14, 15, "MB");

    private static final CourseOffering CSC148_F = new CourseOffering("CSC148H1", Placement.fallWinter(2026, Section.F),
            List.of(
                    activity("LEC0101", LEC, meeting(fall(2026), DayOfWeek.MONDAY, 13, 14, "PB")),
                    activity("TUT0101", TUT, meeting(fall(2026), DayOfWeek.MONDAY, 13, 14, "BA")),
                    activity("TUT0102", TUT, meeting(fall(2026), DayOfWeek.TUESDAY, 13, 14, "BA"))));

    private static final CourseOffering ENG140_Y = new CourseOffering("ENG140Y1", Placement.fallWinter(2026, Section.Y),
            List.of(activity("LEC0101", LEC, ENG140_FALL, ENG140_WINTER)));

    private static final CourseOffering CSC108_S = new CourseOffering("CSC108H1", Placement.fallWinter(2026, Section.S),
            List.of(
                    activity("LEC0201", LEC, CSC108_WINTER),
                    activity("PRA0101", PRA, meeting(winter(2027), DayOfWeek.THURSDAY, 9, 10, "BA"))));

    private static final CourseOffering ANT100_Y = new CourseOffering("ANT100Y1", Placement.fallWinter(2026, Section.Y),
            List.of(activity("LEC0201", LEC)));

    private static MeetingTime meeting(AcademicTerm term, DayOfWeek day, int startHour, int endHour, String location) {
        return new MeetingTime(term, day, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0), location);
    }

    private static Activity activity(String code, ActivityType type, MeetingTime... meetings) {
        return new Activity(code, type, List.of(meetings), List.of());
    }

    private static ScheduledCourse scheduled(CourseOffering offering, Map<ActivityType, String> choice) {
        return new ScheduledCourse(offering, new TimetableChoice(choice));
    }

    @Test
    void lectureTutorialClash() {
        Timetable t = new Timetable(List.of(scheduled(CSC148_F, Map.of(LEC, "LEC0101", TUT, "TUT0101"))));
        List<Conflict> conflicts = t.conflicts();
        assertEquals(1, conflicts.size());
        assertEquals("LEC0101", conflicts.get(0).getFirst().getActivityCode());
        assertEquals("TUT0101", conflicts.get(0).getSecond().getActivityCode());
        assertEquals(fall(2026), conflicts.get(0).getTerm());
    }

    @Test
    void differentTutorialAvoidsClash() {
        Timetable t = new Timetable(List.of(scheduled(CSC148_F, Map.of(LEC, "LEC0101", TUT, "TUT0102"))));
        assertTrue(t.conflicts().isEmpty());
    }

    @Test
    void fullYearLectureClashesWithWinterCourse() {
        Timetable t = new Timetable(List.of(
                scheduled(ENG140_Y, Map.of(LEC, "LEC0101")),
                scheduled(CSC108_S, Map.of(LEC, "LEC0201", PRA, "PRA0101"))));
        ScheduledMeeting eng = new ScheduledMeeting("ENG140Y1", "LEC0101", LEC, ENG140_WINTER);
        ScheduledMeeting csc = new ScheduledMeeting("CSC108H1", "LEC0201", LEC, CSC108_WINTER);
        assertEquals(List.of(new Conflict(eng, csc)), t.conflicts());
    }

    @Test
    void fullYearClashReportedOncePerTerm() {
        CourseOffering other = new CourseOffering("PHL101Y1", Placement.fallWinter(2026, Section.Y),
                List.of(activity("LEC0101", LEC,
                        meeting(fall(2026), DayOfWeek.FRIDAY, 14, 16, "SS"),
                        meeting(winter(2027), DayOfWeek.FRIDAY, 14, 16, "SS"))));
        Timetable t = new Timetable(List.of(
                scheduled(ENG140_Y, Map.of(LEC, "LEC0101")),
                scheduled(other, Map.of(LEC, "LEC0101"))));
        List<Conflict> conflicts = t.conflicts();
        assertEquals(2, conflicts.size());
        assertEquals(fall(2026), conflicts.get(0).getTerm());
        assertEquals(winter(2027), conflicts.get(1).getTerm());
    }

    @Test
    void asynchronousNeverConflicts() {
        Timetable t = new Timetable(List.of(
                scheduled(ANT100_Y, Map.of(LEC, "LEC0201")),
                scheduled(ENG140_Y, Map.of(LEC, "LEC0101"))));
        assertTrue(t.conflicts().isEmpty());
        assertTrue(t.meetingsIn(fall(2026)).stream().noneMatch(m -> m.getCourseCode().equals("ANT100Y1")));
    }

    @Test
    void missingTutorialChoice() {
        Timetable t = new Timetable(List.of(
                scheduled(CSC148_F, Map.of(LEC, "LEC0101")),
                scheduled(ENG140_Y, Map.of(LEC, "LEC0101"))));
        assertEquals(Map.of("CSC148H1", Set.of(TUT)), t.missingChoices());
    }

    @Test
    void staleChoice() {
        Timetable t = new Timetable(List.of(
                scheduled(CSC148_F, Map.of(LEC, "LEC0101", TUT, "TUT9999")),
                scheduled(CSC108_S, Map.of(LEC, "PRA0101", PRA, "PRA0101"))));
        assertEquals(Map.of("CSC148H1", Map.of(TUT, "TUT9999"), "CSC108H1", Map.of(LEC, "PRA0101")),
                t.staleChoices());
    }

    @Test
    void staleChoiceHasNoMeetings() {
        Timetable t = new Timetable(List.of(scheduled(CSC148_F, Map.of(LEC, "TUT0101", TUT, "TUT9999"))));
        assertTrue(t.meetingsIn(fall(2026)).isEmpty());
    }

    @Test
    void threeWayClashGivesThreePairs() {
        CourseOffering other = new CourseOffering("MAT137Y1", Placement.fallWinter(2026, Section.Y),
                List.of(activity("LEC0101", LEC, meeting(fall(2026), DayOfWeek.MONDAY, 13, 14, "MP"))));
        Timetable t = new Timetable(List.of(
                scheduled(CSC148_F, Map.of(LEC, "LEC0101", TUT, "TUT0101")),
                scheduled(other, Map.of(LEC, "LEC0101"))));
        assertEquals(3, t.conflicts().size());
    }

    @Test
    void emptyTimetable() {
        Timetable t = new Timetable(List.of());
        assertTrue(t.termsCovered().isEmpty());
        assertTrue(t.meetingsIn(fall(2026)).isEmpty());
        assertTrue(t.conflicts().isEmpty());
        assertTrue(t.missingChoices().isEmpty());
        assertTrue(t.staleChoices().isEmpty());
    }

    @Test
    void copiesItsList() {
        List<ScheduledCourse> courses = new ArrayList<>(List.of(scheduled(CSC148_F, Map.of())));
        Timetable t = new Timetable(courses);
        courses.clear();
        assertEquals(1, t.getCourses().size());
    }

    @Test
    void meetingsInWinterIncludeYCourse() {
        Timetable t = new Timetable(List.of(
                scheduled(ENG140_Y, Map.of(LEC, "LEC0101")),
                scheduled(CSC148_F, Map.of(LEC, "LEC0101"))));
        assertEquals(List.of(new ScheduledMeeting("ENG140Y1", "LEC0101", LEC, ENG140_WINTER)),
                t.meetingsIn(winter(2027)));
    }

    @Test
    void termsCoveredSorted() {
        Timetable t = new Timetable(List.of(
                scheduled(CSC108_S, Map.of()),
                scheduled(CSC148_F, Map.of()),
                scheduled(ENG140_Y, Map.of())));
        assertEquals(List.of(fall(2026), winter(2027)), t.termsCovered());
    }
}
