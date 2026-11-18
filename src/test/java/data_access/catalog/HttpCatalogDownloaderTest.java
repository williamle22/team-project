package data_access.catalog;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.sync_catalog.CatalogUnavailableException;
import use_case.sync_catalog.DownloadedCatalog;
import use_case.sync_catalog.FetchResult;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class HttpCatalogDownloaderTest {
    private static final String LAST_MODIFIED = "Thu, 12 Nov 2026 10:00:04 GMT";
    private static final Instant SERVER_TIME = Instant.parse("2026-11-12T10:00:04Z");

    @TempDir
    Path dir;

    private final Deque<Reply> replies = new ArrayDeque<>();
    private final List<String> ifModifiedSince = new ArrayList<>();
    private HttpServer server;
    private HttpCatalogDownloader downloader;

    private static final class Reply {
        final int status;
        final String body;
        final Map<String, String> headers;

        Reply(int status, String body, Map<String, String> headers) {
            this.status = status;
            this.body = body;
            this.headers = headers;
        }
    }

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/db", exchange -> {
            ifModifiedSince.add(exchange.getRequestHeaders().getFirst("If-Modified-Since"));
            Reply reply = replies.remove();
            reply.headers.forEach((k, v) -> exchange.getResponseHeaders().set(k, v));
            if (reply.body == null) {
                exchange.sendResponseHeaders(reply.status, -1);
            } else {
                byte[] bytes = reply.body.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(reply.status, bytes.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                }
            }
            exchange.close();
        });
        server.start();
        downloader = new HttpCatalogDownloader("http://127.0.0.1:" + server.getAddress().getPort(), dir);
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private List<Path> partFiles() throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.getFileName().toString().endsWith(".db.part")).collect(Collectors.toList());
        }
    }

    @Test
    void downloads200() throws Exception {
        replies.add(new Reply(200, "catalog bytes", Map.of("Last-Modified", LAST_MODIFIED)));
        FetchResult result = downloader.fetchIfNewer(Optional.empty());

        assertEquals(FetchResult.Kind.DOWNLOADED, result.getKind());
        DownloadedCatalog d = result.getDownload().orElseThrow();
        assertEquals(dir, d.getFile().getParent());
        assertEquals("catalog bytes", Files.readString(d.getFile()));
        assertEquals(Optional.of(SERVER_TIME), d.getServerTime());
        assertEquals(List.of(d.getFile()), partFiles());
    }

    @Test
    void missingLastModifiedGivesNoServerTime() throws Exception {
        replies.add(new Reply(200, "catalog bytes", Map.of()));
        DownloadedCatalog d = downloader.fetchIfNewer(Optional.empty()).getDownload().orElseThrow();
        assertEquals(Optional.empty(), d.getServerTime());
    }

    @Test
    void sendsIfModifiedSince() throws Exception {
        replies.add(new Reply(304, null, Map.of()));
        downloader.fetchIfNewer(Optional.of(Instant.parse("2026-11-05T10:00:04Z")));
        assertEquals(List.of("Thu, 05 Nov 2026 10:00:04 GMT"), ifModifiedSince);
    }

    @Test
    void noTimeSendsNoHeader() throws Exception {
        replies.add(new Reply(200, "catalog bytes", Map.of()));
        downloader.fetchIfNewer(Optional.empty());
        assertNull(ifModifiedSince.get(0));
    }

    @Test
    void notModified304() throws Exception {
        replies.add(new Reply(304, null, Map.of()));
        FetchResult result = downloader.fetchIfNewer(Optional.of(SERVER_TIME));
        assertEquals(FetchResult.notModified(), result);
        assertTrue(partFiles().isEmpty());
    }

    @Test
    void retriesOnce429() throws Exception {
        replies.add(new Reply(429, "slow down", Map.of("Retry-After", "0")));
        replies.add(new Reply(200, "catalog bytes", Map.of()));
        FetchResult result = downloader.fetchIfNewer(Optional.empty());

        assertEquals(FetchResult.Kind.DOWNLOADED, result.getKind());
        assertEquals(2, ifModifiedSince.size());
        assertEquals(List.of(result.getDownload().orElseThrow().getFile()), partFiles());
    }

    @Test
    void waitsRetryAfterSeconds() throws Exception {
        replies.add(new Reply(429, "slow down", Map.of("Retry-After", "1")));
        replies.add(new Reply(304, null, Map.of()));
        long start = System.nanoTime();
        downloader.fetchIfNewer(Optional.of(SERVER_TIME));
        long ms = (System.nanoTime() - start) / 1_000_000;
        assertTrue(ms >= 1000, "waited only " + ms + " ms");
    }

    @Test
    void twice429Throws() throws IOException {
        replies.add(new Reply(429, "slow down", Map.of("Retry-After", "0")));
        replies.add(new Reply(429, "slow down", Map.of("Retry-After", "0")));
        CatalogUnavailableException e =
                assertThrows(CatalogUnavailableException.class, () -> downloader.fetchIfNewer(Optional.empty()));
        assertEquals("server returned 429", e.getMessage());
        assertEquals(2, ifModifiedSince.size());
        assertTrue(partFiles().isEmpty());
    }

    @Test
    void serverError500Throws() throws IOException {
        replies.add(new Reply(500, "boom", Map.of()));
        CatalogUnavailableException e =
                assertThrows(CatalogUnavailableException.class, () -> downloader.fetchIfNewer(Optional.empty()));
        assertEquals("server returned 500", e.getMessage());
        assertTrue(partFiles().isEmpty());
    }

    @Test
    void connectionRefusedThrows() throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        HttpCatalogDownloader offline = new HttpCatalogDownloader("http://127.0.0.1:" + port, dir);
        CatalogUnavailableException e =
                assertThrows(CatalogUnavailableException.class, () -> offline.fetchIfNewer(Optional.empty()));
        assertEquals("network error", e.getMessage());
        assertTrue(partFiles().isEmpty());
    }
}
