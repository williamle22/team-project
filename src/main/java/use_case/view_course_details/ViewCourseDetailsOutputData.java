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

}