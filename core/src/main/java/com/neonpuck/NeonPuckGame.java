package com.neonpuck;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class NeonPuckGame extends Game {
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new SplashScreen(this)); // abre na splash
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        super.dispose();
    }
}
