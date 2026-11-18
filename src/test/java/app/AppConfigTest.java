package app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @AfterEach
    void clearOverrides() {
        System.clearProperty("dgx.url");
        System.clearProperty("ttb.url");
        System.clearProperty("dgx.data");
    }

    @Test
    void readsUrlsFromFile() {
        AppConfig config = AppConfig.load();
        assertTrue(config.dgxUrl().startsWith("https://"));
        assertTrue(config.ttbUrl().startsWith("https://"));
        assertFalse(config.dgxUrl().endsWith("/"));
        assertFalse(config.ttbUrl().endsWith("/"));
    }

    @Test
    void systemPropertyOverridesFile() {
        System.setProperty("dgx.url", "http://localhost:8080");
        System.setProperty("ttb.url", "http://localhost:9090/ttb");
        AppConfig config = AppConfig.load();
        assertEquals("http://localhost:8080", config.dgxUrl());
        assertEquals("http://localhost:9090/ttb", config.ttbUrl());
    }

    @Test
    void trailingSlashIsRemoved() {
        System.setProperty("dgx.url", "http://localhost:8080/");
        assertEquals("http://localhost:8080", AppConfig.load().dgxUrl());
    }

    @Test
    void dataDirectoryDefaultsToDataFolder() {
        Path dir = AppConfig.load().dataDirectory();
        assertTrue(dir.isAbsolute());
        assertEquals(Paths.get("data").toAbsolutePath(), dir);
    }

    @Test
    void systemPropertyOverridesDataDirectory(@TempDir Path tmp) {
        System.setProperty("dgx.data", tmp.toString());
        assertEquals(tmp.toAbsolutePath(), AppConfig.load().dataDirectory());
    }
}
