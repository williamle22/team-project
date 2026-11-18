package app;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

/** Starts the app: installs the FlatLaf look and feel, opens the main window, and syncs the catalog. */
public final class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            AppBuilder builder = new AppBuilder().addCatalogSync();
            JFrame frame = builder.build();
            frame.setVisible(true);
            builder.startCatalogSync();
        });
    }
}
