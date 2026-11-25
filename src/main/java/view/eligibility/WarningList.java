package view.eligibility;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Shows warnings, one per line, each with a bullet. It hides itself when there are no warnings. */
public final class WarningList extends JLabel {
    private static final Color WARNING_COLOUR = new Color(0xB26A00);

    public WarningList() {
        setForeground(WARNING_COLOUR);
        setVisible(false);
    }

    public void setWarnings(List<String> warnings) {
        setText(html(warnings));
        setVisible(!warnings.isEmpty());
    }

    static String html(List<String> warnings) {
        StringBuilder sb = new StringBuilder("<html>");

        for (String w : warnings) {
            sb.append("&#8226; ").append(escape(w)).append("<br>");
        }

        return sb.append("</html>").toString();
    }

    static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
