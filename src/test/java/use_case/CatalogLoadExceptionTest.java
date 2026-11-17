package use_case;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CatalogLoadExceptionTest {

    @Test
    void keepsMessage() {
        CatalogLoadException e = new CatalogLoadException("no version row");
        assertEquals("no version row", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void keepsMessageAndCause() {
        IOException cause = new IOException("disk error");
        CatalogLoadException e = new CatalogLoadException("could not read catalog", cause);
        assertEquals("could not read catalog", e.getMessage());
        assertSame(cause, e.getCause());
    }
}
