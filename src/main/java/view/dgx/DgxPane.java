package view.dgx;

import javax.swing.JSplitPane;
import java.awt.Color;

// TODO: split pane consisting of CourseSearch and AcademicPlanner
public class DgxPane extends JSplitPane {
    private final CourseSearchPanel courseSearchPanel;
    private final AcademicPlannerPanel academicPlannerPanel;

    public DgxPane(CourseSearchPanel courseSearchPanel, AcademicPlannerPanel academicPlannerPanel) {
        super(JSplitPane.HORIZONTAL_SPLIT);

        this.courseSearchPanel = courseSearchPanel;
        this.academicPlannerPanel = academicPlannerPanel;

        setLeftComponent(courseSearchPanel);
        setRightComponent(academicPlannerPanel);

        setResizeWeight(0.25);
        setDividerLocation(250);
        setDividerSize(8);
        setBackground(Color.MAGENTA);
    }
}
