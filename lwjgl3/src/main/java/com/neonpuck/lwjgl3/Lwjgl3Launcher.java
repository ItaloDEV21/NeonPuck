package com.neonpuck.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.neonpuck.NeonPuckGame;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Neon Puck ⚡ (LibGDX)");
        config.setWindowedMode(1000, 560);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new NeonPuckGame(), config);
    }
}
