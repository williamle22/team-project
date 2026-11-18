package entity.requirement;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileReqTest {

    @Test
    void gettersReturnConstructorValues() {
        ProfileReq r = new ProfileReq("min_year_of_study", 2, "Second year standing", true);
        assertEquals("min_year_of_study", r.getAttr());
        assertEquals(2, r.getValue());
        assertEquals("Second year standing", r.getText());
        assertEquals(ProfileComparison.MIN, r.getComparison());
        assertTrue(r.isConcurrent());
    }

    @Test
    void typedGettersReturnValue() {
        assertEquals(2, new ProfileReq("min_year_of_study", 2, "", false).getIntValue());
        assertTrue(new ProfileReq("has_transfer_credits", true, "", false).getBooleanValue());
        assertEquals("ArtSci", new ProfileReq("faculty", "ArtSci", "", false).getStringValue());
    }

    @Test
    void typedGettersCheckType() {
        ProfileReq r = new ProfileReq("has_transfer_credits", true, "", false);
        assertThrows(IllegalStateException.class, r::getIntValue);
        assertThrows(IllegalStateException.class, r::getStringValue);
        assertThrows(IllegalStateException.class, () -> new ProfileReq("faculty", "ArtSci", "", false).getBooleanValue());
    }

    @Test
    void rejectsDoubleValue() {
        assertThrows(IllegalArgumentException.class, () -> new ProfileReq("min_year_of_study", 2.0, "", false));
    }

    @Test
    void rejectsNullValue() {
        assertThrows(IllegalArgumentException.class, () -> new ProfileReq("faculty", null, "", false));
    }

    @Test
    void rejectsNullAttr() {
        assertThrows(NullPointerException.class, () -> new ProfileReq(null, 2, "", false));
    }

    @Test
    void rejectsNullText() {
        assertThrows(NullPointerException.class, () -> new ProfileReq("faculty", "ArtSci", null, false));
    }

    @Test
    void equalsComparesConcurrent() {
        assertEquals(new ProfileReq("faculty", "ArtSci", "", false), new ProfileReq("faculty", "ArtSci", "", false));
        assertEquals(new ProfileReq("faculty", "ArtSci", "", false).hashCode(),
                new ProfileReq("faculty", "ArtSci", "", false).hashCode());
        assertNotEquals(new ProfileReq("faculty", "ArtSci", "", false), new ProfileReq("faculty", "ArtSci", "", true));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(ProfileReq.class).usingGetClass().withRedefinedSuperclass()
                .suppress(Warning.NULL_FIELDS).verify();
    }
}
