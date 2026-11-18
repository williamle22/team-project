package data_access.catalog;

import use_case.sync_catalog.CatalogSource;
import use_case.sync_catalog.CatalogUnavailableException;
import use_case.sync_catalog.DownloadedCatalog;
import use_case.sync_catalog.FetchResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public final class HttpCatalogDownloader implements CatalogSource {
    private static final DateTimeFormatter HTTP_DATE =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).withZone(ZoneOffset.UTC);

    private final String baseUrl;
    private final Path downloadDirectory;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public HttpCatalogDownloader(String baseUrl, Path downloadDirectory) {
        this.baseUrl = baseUrl;
        this.downloadDirectory = downloadDirectory;
    }

    @Override
    public FetchResult fetchIfNewer(Optional<Instant> cachedTime) throws CatalogUnavailableException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + "/db"))
                .timeout(Duration.ofSeconds(60))
                .GET();

        cachedTime.ifPresent(instant -> builder.header("If-Modified-Since", HTTP_DATE.format(instant)));

        HttpRequest request = builder.build();

        try {
            HttpResponse<Path> resp = send(request);

            if (resp.statusCode() == 429) {
                Files.deleteIfExists(resp.body());
                Thread.sleep(retryAfterSeconds(resp) * 1000L);
                resp = send(request);
            }

            return toResult(resp);
        } catch (IOException e) {
            throw new CatalogUnavailableException("network error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CatalogUnavailableException("interrupted", e);
        }
    }

    private HttpResponse<Path> send(HttpRequest request) throws IOException, InterruptedException {
        Path tmp = Files.createTempFile(downloadDirectory, "dgx-", ".db.part");

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofFile(tmp));
        } catch (IOException | InterruptedException e) {
            Files.deleteIfExists(tmp);
            throw e;
        }
    }

    private FetchResult toResult(HttpResponse<Path> resp) throws IOException, CatalogUnavailableException {
        switch (resp.statusCode()) {
            case 304:
                Files.deleteIfExists(resp.body());
                return FetchResult.notModified();
            case 200:
                return FetchResult.downloaded(new DownloadedCatalog(resp.body(), lastModified(resp)));
            default:
                Files.deleteIfExists(resp.body());
                throw new CatalogUnavailableException("server returned " + resp.statusCode());
        }
    }

    private static long retryAfterSeconds(HttpResponse<Path> resp) {
        return resp.headers().firstValue("Retry-After").map(Long::parseLong).orElse(1L);
    }

    private static Instant lastModified(HttpResponse<Path> resp) {
        return resp.headers().firstValue("Last-Modified")
                .map(v -> ZonedDateTime.parse(v, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant())
                .orElse(null);
    }
}
