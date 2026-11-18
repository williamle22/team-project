package view;

import view.dgx.DgxPane;
import view.timetable.TimeTablePane;

import javax.swing.JRootPane;

public class HomePane extends JRootPane {
    private DgxPane dgxPane; // TODO: should this be a view abstraction
    private TimeTablePane timeTablePane;
}
