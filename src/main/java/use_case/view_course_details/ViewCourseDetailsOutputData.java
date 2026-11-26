package use_case.view_course_details;

import java.util.List;
import java.util.Objects;

public final class ViewCourseDetailsOutputData{
    private final String code;
    private final String title;
    private final String description;
    private final double weight;
    private final List<String> breadth;
    private final List<String> fallWinterSections;
    private final List<String> summerSections;
    private final RequirementText prerequisites;
    private final RequirementText corequisites;
    private final RequirementText exclusions;

    public ViewCourseDetailsOutputData(String code, String title, String description, double weight,
                                   List<String> breadth, List<String> fallWinterSections,
                                   List<String> summerSections, RequirementText prerequisites,
                                   RequirementText corequisites, RequirementText exclusions){
        this.code = Objects.requireNonNull(code, "code");
        this.title = Objects.requireNonNull(title, "title");
        this.description = Objects.requireNonNull(description, "description");
        this.weight = weight;
        this.breadth = List.copyOf(breadth);
        this.fallWinterSections = List.copyOf(fallWinterSections);
        this.summerSections = List.copyOf(summerSections);
        this.prerequisites = Objects.requireNonNull(prerequisites, "prerequisites");
        this.corequisites = Objects.requireNonNull(corequisites, "corequisites");
        this.exclusions = Objects.requireNonNull(exclusions, "exclusions");
    }

    public String getCode(){
        return code;
    }

    public String getTitle(){
        return title;
    }

    public String getDescription(){
        return description;
    }

    public double getWeight(){
        return weight;
    }

    public List<String> getBreadth(){
        return breadth;
    }

    public List<String> getFallWinterSections(){
        return fallWinterSections;
    }

    public List<String> getSummerSections(){
        return summerSections;
    }

    public RequirementText getPrerequisites(){
        return prerequisites;
    }

    public RequirementText getCorequisites() {
        return corequisites;
    }

    public RequirementText getExclusions() {
        return exclusions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ViewCourseDetailsOutputData)) {
            return false;
        }
        ViewCourseDetailsOutputData that = (ViewCourseDetailsOutputData) o;
        return Double.compare(weight, that.weight) == 0 && code.equals(that.code) && title.equals(that.title)
                && description.equals(that.description) && breadth.equals(that.breadth)
                && fallWinterSections.equals(that.fallWinterSections)
                && summerSections.equals(that.summerSections) && prerequisites.equals(that.prerequisites)
                && corequisites.equals(that.corequisites) && exclusions.equals(that.exclusions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, title, description, weight, breadth, fallWinterSections, summerSections,
                prerequisites, corequisites, exclusions);
    }

    @Override
    public String toString() {
        return "ViewCourseDetailsOutputData{" + code + ", " + title + "}";
    }
}