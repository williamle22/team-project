package view.eligibility;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarningListTest {

    @Test
    void escapesHtml() {
        assertEquals("a &lt;b&gt; &amp; c", WarningList.escape("a <b> & c"));
    }

    @Test
    void showsOneLinePerWarning() {
        assertEquals("<html>&#9888; a<br>&#9888; b &lt; c<br></html>", WarningList.html(List.of("a", "b < c")));
    }

    @Test
    void hiddenWhenEmpty() {
        WarningList list = new WarningList();

        list.setWarnings(List.of("CSC108H1 appears 2 times (retake?)"));
        assertTrue(list.isVisible());

        list.setWarnings(List.of());
        assertFalse(list.isVisible());
    }
}
