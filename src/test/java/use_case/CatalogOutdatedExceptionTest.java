package use_case;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CatalogOutdatedExceptionTest {

    @Test
    void keepsMessage() {
        CatalogOutdatedException e = new CatalogOutdatedException("catalog schema 6 is outdated");
        assertEquals("catalog schema 6 is outdated", e.getMessage());
        assertNull(e.getCause());
        assertInstanceOf(CatalogLoadException.class, e);
    }

    @Test
    void keepsMessageAndCause() {
        IOException cause = new IOException("disk error");
        CatalogOutdatedException e = new CatalogOutdatedException("catalog schema 6 is outdated", cause);
        assertEquals("catalog schema 6 is outdated", e.getMessage());
        assertSame(cause, e.getCause());
    }
}
