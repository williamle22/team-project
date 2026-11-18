package use_case.sync_catalog;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CatalogUnavailableExceptionTest {

    @Test
    void keepsMessage() {
        CatalogUnavailableException e = new CatalogUnavailableException("server returned 503");
        assertEquals("server returned 503", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void keepsMessageAndCause() {
        IOException cause = new IOException("connection refused");
        CatalogUnavailableException e = new CatalogUnavailableException("could not reach server", cause);
        assertEquals("could not reach server", e.getMessage());
        assertSame(cause, e.getCause());
    }
}
