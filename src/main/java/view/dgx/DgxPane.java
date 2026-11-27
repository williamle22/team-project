package view.dgx;

import javax.swing.JSplitPane;
import view.SearchPanel;

// Split pane consisting of the main course search and academic planner.
public class DgxPane extends JSplitPane {
    private final SearchPanel searchPanel;
    private final AcademicPlannerPanel academicPlannerPanel;

    public DgxPane(SearchPanel searchPanel, AcademicPlannerPanel academicPlannerPanel) {
        super(JSplitPane.HORIZONTAL_SPLIT);

        this.searchPanel = searchPanel;
        this.academicPlannerPanel = academicPlannerPanel;

        setLeftComponent(searchPanel);
        setRightComponent(academicPlannerPanel);

        setResizeWeight(0.25);
        setDividerLocation(250);
        setDividerSize(8);
    }
}
