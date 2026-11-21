package view.eligibility;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StatusBadgeTest {

    @Test
    void eachStatusHasItsOwnColour() {
        Color met = StatusBadge.colourFor("met");
        Color notMet = StatusBadge.colourFor("not met");
        Color review = StatusBadge.colourFor("review");

        assertNotEquals(met, notMet);
        assertNotEquals(met, review);
        assertNotEquals(notMet, review);
        assertEquals(StatusBadge.colourFor(""), StatusBadge.colourFor("unknown"));
    }

    @Test
    void setStatusSetsTooltip() {
        StatusBadge badge = new StatusBadge();

        badge.setStatus("not met");

        assertEquals("not met", badge.getStatus());
        assertEquals("Requirements not met", badge.getToolTipText());
        assertEquals("Requirements met", StatusBadge.tooltipFor("met"));
        assertEquals("Needs review", StatusBadge.tooltipFor("review"));
        assertNull(StatusBadge.tooltipFor(""));
    }

    @Test
    void hasFixedSize() {
        assertEquals(new Dimension(12, 12), new StatusBadge().getPreferredSize());
        assertEquals(10, new StatusIcon("met", 10).getIconWidth());
        assertEquals(10, new StatusIcon("met", 10).getIconHeight());
    }
}
