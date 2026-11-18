package app;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * The app's settings, read from {@code config.properties} on the classpath. A system property with the same key, such
 * as {@code -Ddgx.url=...}, overrides the file. A missing file or key throws {@link IllegalStateException}.
 */
public final class AppConfig {
    private final Properties properties;

    private AppConfig(Properties properties) {
        this.properties = properties;
    }

    public static AppConfig load() {
        try (InputStream in = AppConfig.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties is not on the classpath");
            }

            Properties properties = new Properties();
            properties.load(in);

            return new AppConfig(properties);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Returns the dgx-backend base URL, without a trailing slash. */
    public String dgxUrl() {
        return url("dgx.url");
    }

    /** Returns the Timetable Builder (EASI) base URL, without a trailing slash. */
    public String ttbUrl() {
        return url("ttb.url");
    }

    /**
     * Returns the absolute path of the folder for the catalog cache and saved plans. That's {@code -Ddgx.data} if it's
     * set, and otherwise {@code data} in the working directory.
     */
    public Path dataDirectory() {
        return Paths.get(System.getProperty("dgx.data", "data")).toAbsolutePath();
    }

    private String url(String key) {
        String url = System.getProperty(key, properties.getProperty(key));

        if (url == null) {
            throw new IllegalStateException("no " + key + " in config.properties");
        }

        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
