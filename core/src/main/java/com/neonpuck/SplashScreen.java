package com.neonpuck;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SplashScreen implements Screen {
    private final NeonPuckGame game;
    private final OrthographicCamera cam;
    private final ShapeRenderer sr;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private final int W = 1000, H = 560;
    private float t = 0f;

    public SplashScreen(NeonPuckGame game){
        this.game = game;
        cam = new OrthographicCamera();
        cam.setToOrtho(false, W, H);
        sr = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.2f);
    }

    @Override public void render(float delta) {
        t += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new PlayScreen(game));
            dispose();
            return;
        }

        Gdx.gl.glClearColor(0.04f, 0.05f, 0.14f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        cam.update();
        sr.setProjectionMatrix(cam.combined);

        // fundo e círculo pulsante
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.04f,0.05f,0.14f,1f);
        sr.rect(0,0,W,H);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        float pulse = 80 + (float)Math.sin(t*2.0f)*6f;
        sr.setColor(139/255f,233/255f,253/255f,0.8f);
        sr.circle(W/2f, H/2f, pulse);
        sr.circle(W/2f, H/2f, pulse*0.65f);
        sr.end();

        // textos
        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();

        font.setColor(0.55f,0.95f,1f,1f);
        font.getData().setScale(2.0f);
        String title = "NEON PUCK";
        layout.setText(font, title);
        font.draw(game.batch, title, W/2f - layout.width/2f, H/2f + 40);

        font.getData().setScale(1.0f);
        font.setColor(0.95f,0.98f,0.55f, 0.7f + 0.3f*(float)Math.sin(t*6.0f)); // pisca
        String press = "Pressione ENTER para jogar";
        layout.setText(font, press);
        font.draw(game.batch, press, W/2f - layout.width/2f, H/2f - 10);

        font.getData().setScale(0.8f);
        font.setColor(0.78f,0.86f,1f,0.9f);
        font.draw(game.batch, "Controles: WASD/Setas • A = alterna arena • 1/2/3 = IA • Espaco = Pausa", 20, 24);

        game.batch.end();
    }

    @Override public void show() { }
    @Override public void resize(int width, int height) { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override public void dispose() {
        sr.dispose();
        font.dispose();
    }
}
