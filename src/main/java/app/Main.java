package app;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

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
