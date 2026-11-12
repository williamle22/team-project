package use_case;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PlanStorageExceptionTest {

    @Test
    void keepsMessageAndCause() {
        IOException cause = new IOException("disk full");
        PlanStorageException e = new PlanStorageException("could not save plan", cause);
        assertEquals("could not save plan", e.getMessage());
        assertSame(cause, e.getCause());
    }
}
