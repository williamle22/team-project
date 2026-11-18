package interface_adapter.sync_catalog;

import org.junit.jupiter.api.Test;
import use_case.sync_catalog.SyncCatalogOutputBoundary;
import use_case.sync_catalog.SyncCatalogOutputData;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SyncCatalogControllerTest {

    private static final class FailRecorder implements SyncCatalogOutputBoundary {
        final CompletableFuture<String> failMessage = new CompletableFuture<>();

        @Override
        public void prepareLoadingView() {
        }

        @Override
        public void prepareSuccessView(SyncCatalogOutputData data) {
        }

        @Override
        public void prepareFailView(String message) {
            failMessage.complete(message);
        }
    }

    @Test
    void runsInteractorOnAnotherThread() throws Exception {
        CompletableFuture<Thread> ranOn = new CompletableFuture<>();
        new SyncCatalogController(() -> ranOn.complete(Thread.currentThread()), new FailRecorder()).execute();
        assertNotSame(Thread.currentThread(), ranOn.get(5, TimeUnit.SECONDS));
    }

    @Test
    void unexpectedErrorShowsFailView() throws Exception {
        FailRecorder presenter = new FailRecorder();
        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        try {
            new SyncCatalogController(() -> {
                throw new IllegalStateException("bug in sync");
            }, presenter).execute();
            assertEquals("Something went wrong loading course data.", presenter.failMessage.get(5, TimeUnit.SECONDS));
        } finally {
            System.setErr(originalErr);
        }
        assertTrue(err.toString().contains("IllegalStateException: bug in sync"));
    }
}
