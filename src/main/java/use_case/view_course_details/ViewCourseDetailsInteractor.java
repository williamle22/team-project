package use_case.view_course_details;

import entity.catalog.BreadthCategory;
import entity.catalog.Course;
import entity.catalog.ExclusionItem;
import entity.catalog.Exclusions;
import entity.catalog.Requisite;
import entity.catalog.Section;
import entity.catalog.Sessions;
import entity.requirement.RequirementPrinter;
import use_case.CatalogReader;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


    /** Use Case 3: looks up one course and copies its details into output data. */
    public final class ViewCourseDetailsInteractor implements ViewCourseDetailsInputBoundary {
        private final CatalogReader catalog;
        private final ViewCourseDetailsOutputBoundary presenter;
        private final RequirementPrinter printer = new RequirementPrinter();

        public ViewCourseDetailsInteractor(CatalogReader catalog, ViewCourseDetailsOutputBoundary presenter) {
            this.catalog = catalog;
            this.presenter = presenter;
        }

        @Override
        public void execute(ViewCourseDetailsInputData input) {
            if (!catalog.isLoaded()) {
                presenter.prepareFailView("Course data is still loading.");
                return;
            }

            Optional<Course> found = catalog.findCourse(input.getCourseCode());
            if (found.isEmpty()) {
                presenter.prepareFailView(input.getCourseCode() + " isn't in the current calendar.");
                return;
            }

            Course course = found.get();
            List<String> breadth = course.getBreadth().stream()
                    .map(b -> b.getDisplayName() + " (" + b.getNumber() + ")")
                    .collect(Collectors.toList());

            Optional<Sessions> sessions = course.getSessions();
            List<String> fallWinter = sessions.map(s -> sectionValues(s.getFallWinter())).orElse(List.of());
            List<String> summer = sessions.map(s -> sectionValues(s.getSummer())).orElse(List.of());

            presenter.prepareSuccessView(new ViewCourseDetailsOutputBoundary(
                    course.getCode(), course.getTitle(), course.getDescription(), course.getWeight(),
                    breadth, fallWinter, summer,
                    requisiteText(course.getPrerequisites()),
                    requisiteText(course.getCorequisites()),
                    exclusionText(course.getExclusions())) {
                @Override
                public void prepareSuccessView(ViewCourseDetailsOutputBoundary data) {

                }

                @Override
                public void prepareFailView(String message) {

                }
            });
        }

        private static List<String> sectionValues(List<Section> sections) {
            return sections.stream().map(Section::getValue).collect(Collectors.toList());
        }

        private RequirementText requisiteText(Optional<Requisite> requisite) {
            return requisite.map(r -> new RequirementText(
                            r.getReq().map(printer::print).orElse(""), r.getRaw(), r.getNotes()))
                    .orElse(RequirementText.none());
        }

        private static RequirementText exclusionText(Optional<Exclusions> exclusions) {
            return exclusions.map(e -> new RequirementText(
                            e.getItems().stream().map(ViewCourseDetailsInteractor::exclusionLine)
                                    .collect(Collectors.joining("; ")),
                            e.getRaw(), e.getNotes()))
                    .orElse(RequirementText.none());
        }

        private static String exclusionLine(ExclusionItem item) {
            if (item.isText()) {
                return item.getText().get();
            }

            StringBuilder line = new StringBuilder();
            if (item.isCourseSet()) {
                line.append(String.join(" and ", item.getCourses()));
            } else {
                line.append("any ").append(String.join("/", item.getDesignators())).append(" course");
                if (!item.getLevels().isEmpty()) {
                    line.append(" at the ")
                            .append(item.getLevels().stream().map(String::valueOf).collect(Collectors.joining("/")))
                            .append(" level");
                }
                if (!item.getExclude().isEmpty()) {
                    line.append(" except ").append(String.join(", ", item.getExclude()));
                }
            }
            if (item.isConcurrent()) {
                line.append(" (taken concurrently)");
            }
            item.getNote().ifPresent(note -> line.append(" ").append(note));
            return line.toString();
        }
    }

