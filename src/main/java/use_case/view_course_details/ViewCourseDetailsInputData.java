package use_case.view_course_details;

import java.util.Objects;

/** What user typed as input, in order to view the details of a course*/
public final class ViewCourseDetailsInputData {
    private final String courseCode;

    public ViewCourseDetailsInputData(String courseCode){
        this.courseCode = Objects.requireNonNull(courseCode,"courseCode");
    }

    public String getCourseCode(){ return courseCode;}

    @Override
    public boolean equals(Object o){
        if (this == o){
            return true;
        }
        if (!(o instanceof ViewCourseDetailsInputData)){
            return false;
        }
        return equals(((ViewCourseDetailsInputData)o).courseCode);
    }

    @Override
    public int hashCode(){return Objects.hashCode(courseCode);}

    @Override
    public String toString(){
        return "ViewCourseDetailsInputData{" + courseCode + "}";
    }

}
