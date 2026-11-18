package data_access.catalog;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonHelpersTest {

    private static final JSONObject EMPTY = new JSONObject();
    private static final JSONObject NULLS = new JSONObject("{\"k\": null}");

    @Test
    void stringsDefaultsToEmpty() {
        assertEquals(List.of(), JsonHelpers.strings(EMPTY, "k"));
        assertEquals(List.of(), JsonHelpers.strings(NULLS, "k"));
    }

    @Test
    void stringsReadsArray() {
        JSONObject o = new JSONObject("{\"k\": [\"CSC110Y1\", \"CSC111H1\"]}");
        List<String> result = JsonHelpers.strings(o, "k");
        assertEquals(List.of("CSC110Y1", "CSC111H1"), result);
        assertThrows(UnsupportedOperationException.class, () -> result.add("MAT137Y1"));
    }

    @Test
    void intsDefaultsToEmpty() {
        assertEquals(List.of(), JsonHelpers.ints(EMPTY, "k"));
        assertEquals(List.of(), JsonHelpers.ints(NULLS, "k"));
    }

    @Test
    void intsReadsArray() {
        JSONObject o = new JSONObject("{\"k\": [1, 2, 3]}");
        List<Integer> result = JsonHelpers.ints(o, "k");
        assertEquals(List.of(1, 2, 3), result);
        assertThrows(UnsupportedOperationException.class, () -> result.add(4));
    }

    @Test
    void nullableIntReadsValueOrNull() {
        assertNull(JsonHelpers.nullableInt(EMPTY, "k"));
        assertNull(JsonHelpers.nullableInt(NULLS, "k"));
        assertEquals(4, JsonHelpers.nullableInt(new JSONObject("{\"k\": 4}"), "k"));
    }

    @Test
    void nullableIntRejectsWrongType() {
        JSONObject o = new JSONObject("{\"k\": \"four\"}");
        assertThrows(JSONException.class, () -> JsonHelpers.nullableInt(o, "k"));
    }

    @Test
    void nullableDoubleReadsValueOrNull() {
        assertNull(JsonHelpers.nullableDouble(EMPTY, "k"));
        assertNull(JsonHelpers.nullableDouble(NULLS, "k"));
        assertEquals(0.5, JsonHelpers.nullableDouble(new JSONObject("{\"k\": 0.5}"), "k"));
    }

    @Test
    void nullableStringReadsValueOrNull() {
        assertNull(JsonHelpers.nullableString(EMPTY, "k"));
        assertNull(JsonHelpers.nullableString(NULLS, "k"));
        assertEquals("FAS", JsonHelpers.nullableString(new JSONObject("{\"k\": \"FAS\"}"), "k"));
    }

    @Test
    void boolDefaultsToFalse() {
        assertFalse(JsonHelpers.bool(EMPTY, "k"));
        assertFalse(JsonHelpers.bool(NULLS, "k"));
        assertTrue(JsonHelpers.bool(new JSONObject("{\"k\": true}"), "k"));
        assertFalse(JsonHelpers.bool(new JSONObject("{\"k\": false}"), "k"));
    }
}
