package app;

import com.formdev.flatlaf.FlatLightLaf;

public class Main {
    public static void main(String[] args) {
        // Set look and feel to FlatLaf
        FlatLightLaf.setup();

        // Main project entrypoint
        AppBuilder appBuilder = new AppBuilder();
        appBuilder.build();
    }
}