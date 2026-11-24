package use_case.view_course_details;

import java.util.List;
import java.util.Objects;

/** One requirement as text: a readable version, the original calendar text, and any notes. */
public final class RequirementText{
    private final String readable;
    private final String calendarText;
    private final List<String> notes;

    public RequirementText(String readable, String calendarText, List<String> notes){
        this.readable = Objects.requireNonNull(readable, "readable");
        this.calendarText = Objects.requireNonNull(calendarText, "calendarText");
        this.notes = List.copyOf(notes);
    }
}
