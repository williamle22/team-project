package entity.requirement;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextReqTest {

    @Test
    void gettersReturnConstructorValues() {
        TextReq r = new TextReq(TextKind.PERMISSION, "Permission of the instructor", true);
        assertEquals(TextKind.PERMISSION, r.getKind());
        assertEquals("Permission of the instructor", r.getText());
        assertTrue(r.isConcurrent());
    }

    @Test
    void rejectsNullKind() {
        assertThrows(NullPointerException.class, () -> new TextReq(null, "", false));
    }

    @Test
    void rejectsNullText() {
        assertThrows(NullPointerException.class, () -> new TextReq(TextKind.OTHER, null, false));
    }

    @Test
    void equalsComparesConcurrent() {
        assertEquals(new TextReq(TextKind.OTHER, "x", false), new TextReq(TextKind.OTHER, "x", false));
        assertEquals(new TextReq(TextKind.OTHER, "x", false).hashCode(), new TextReq(TextKind.OTHER, "x", false).hashCode());
        assertNotEquals(new TextReq(TextKind.OTHER, "x", false), new TextReq(TextKind.OTHER, "x", true));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(TextReq.class).usingGetClass().withRedefinedSuperclass()
                .suppress(Warning.NULL_FIELDS).verify();
    }
}
