package app;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

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

    public String dgxUrl() {
        return url("dgx.url");
    }

    public String ttbUrl() {
        return url("ttb.url");
    }

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
