package use_case.view_course_details;

import lombok.Getter;

import java.util.List;
import java.util.Objects;

/** One requirement as text: a readable version, the original calendar text, and any notes. */
public final class RequirementText{
    @Getter
    private final String readable;
    private final String calendarText;
    private final List<String> notes;

    public RequirementText(String readable, String calendarText, List<String> notes){
        this.readable = Objects.requireNonNull(readable, "readable");
        this.calendarText = Objects.requireNonNull(calendarText, "calendarText");
        this.notes = List.copyOf(notes);
    }

    public static RequirementText none(){
        return new RequirementText("","", List.of());
    }

    public String getCalendarText() {
        return calendarText;
    }

    public List<String> getNotes(){
        return notes;
    }

    @Override
    public int hashCode(){
        return Objects.hash(readable,calendarText,notes);
    }

    @Override
    public String toString(){
        return "Requirement {"+ readable +", "+calendarText+", "+notes+ "}";
    }
}
