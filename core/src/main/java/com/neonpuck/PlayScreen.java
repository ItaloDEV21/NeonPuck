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

import java.util.Random;

public class PlayScreen implements Screen {
    private final NeonPuckGame game;
    private final OrthographicCamera cam;
    private final ShapeRenderer sr;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private final int W = 1000, H = 560;
    private final float GOAL_W = 120f;
    private final float R_PAD_BASE = 34f;
    private final float R_PUCK = 16f;
    private final float MAX_V = 12f; // puck mais rápido
    private final float FRICTION = 0.996f; // mantém velocidade mais tempo

    private Paddle p1, p2;
    private Puck pk;
    private PowerUp power = null;
    private long nextPowerAt = 0L;
    private final Random rand = new Random();

    private boolean paused = false;
    private ArenaType arena = ArenaType.CLASSIC;
    private String aiLevel = "normal";
    private int scoreL = 0, scoreR = 0;
    private String lastGoalSide = null;

    public PlayScreen(NeonPuckGame game) {
        this.game = game;
        cam = new OrthographicCamera();
        cam.setToOrtho(false, W, H);
        sr = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.2f);
        reset();
    }

    private void reset() {
        p1 = new Paddle(W * 0.15f, H * 0.5f, R_PAD_BASE);
        p2 = new Paddle(W * 0.85f, H * 0.5f, R_PAD_BASE);
        pk = new Puck(W * 0.5f, H * 0.5f, R_PUCK);
        kickoff(rand.nextBoolean());
        nextPowerAt = Time.ms() + 9000;
    }

    private void kickoff(boolean toLeft) {
        pk.x = W * 0.5f;
        pk.y = H * 0.5f;
        float ang = (float) (rand.nextFloat() * Math.PI / 3 - Math.PI / 6 + (toLeft ? Math.PI : 0));
        float spd = 8f + rand.nextFloat() * 3.5f;
        pk.vx = (float) Math.cos(ang) * spd;
        pk.vy = (float) Math.sin(ang) * spd;
        pk.clearTrail();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) paused = !paused;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) aiLevel = "easy";
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) aiLevel = "normal";
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) aiLevel = "hard";
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) arena = ArenaType.next(arena);
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            scoreL = scoreR = 0;
            lastGoalSide = null;
            resetPositions("right");
        }

        if (!paused) update(delta);

        // fundo
        Gdx.gl.glClearColor(0.04f, 0.05f, 0.14f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        cam.update();
        sr.setProjectionMatrix(cam.combined);

        drawArena();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.22f, 1f, 0.08f, 0.9f);
        arc(sr, 0, H / 2f, GOAL_W / 2f, 270, 180);
        arc(sr, W, H / 2f, GOAL_W / 2f, 90, 180);
        sr.end();

        if (power != null) {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            power.draw(sr);
            sr.end();
        }

        sr.begin(ShapeRenderer.ShapeType.Filled);
        pk.draw(sr);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        p1.draw(sr, new Color(80 / 255f, 250 / 255f, 123 / 255f, 1f));
        p2.draw(sr, new Color(255 / 255f, 85 / 255f, 85 / 255f, 1f));
        sr.end();

        // 🟡 Placar + informações
        drawHUD();
    }

    private void drawHUD() {
        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();

        // Placar centralizado
        String score = scoreL + " × " + scoreR;
        layout.setText(font, score);
        font.setColor(0.95f, 0.98f, 0.55f, 1f);
        font.draw(game.batch, score, W / 2f - layout.width / 2f, H - 10f);

        // HUD inferior
        font.getData().setScale(0.85f);
        font.setColor(0.78f, 0.86f, 1f, 0.95f);
        String hud = "Arena: " + arena + "  |  IA: " + aiLevel.toUpperCase() +
                "  |  [1]=Fácil [2]=Normal [3]=Difícil  |  [A]=Arena  |  [Espaço]=Pausa";
        font.draw(game.batch, hud, 15, 22);
        font.getData().setScale(1.2f);

        game.batch.end();
    }

    private void update(float dt) {
        float ax = 0, ay = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) ay += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) ay -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) ax -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) ax += 1;

        float len = (float) Math.hypot(ax, ay);
        float spd = (p1.turbo ? 7.5f : 5.0f) * (p1.slow ? 0.6f : 1f);
        if (len > 0) {
            p1.vx = (ax / len) * spd;
            p1.vy = (ay / len) * spd;
        } else p1.vx = p1.vy = 0;

        // IA com ataque
        float track = switch (aiLevel) {
            case "easy" -> 0.6f;
            case "hard" -> 1.2f;
            default -> 0.9f;
        };
        float targetY = pk.y + pk.vy * ("hard".equals(aiLevel) ? 9 : 6);
        float targetX = pk.x - 50;
        float diffY = targetY - p2.y;
        float diffX = targetX - p2.x;

        if (pk.x > W * 0.55f || "hard".equals(aiLevel)) {
            p2.vx += diffX * 0.005f;
        } else {
            p2.vx *= 0.9f;
        }

        p2.vy += diffY * 0.02f * track;
        p2.vy = clamp(p2.vy, -8f, 8f);
        p2.vx = clamp(p2.vx, -5f, 5f);

        p1.x = clamp(p1.x + p1.vx, 40, W * 0.5f - 60);
        p1.y = clamp(p1.y + p1.vy, 40, H - 40);
        p2.x = clamp(p2.x + p2.vx, W * 0.5f + 60, W - 40);
        p2.y = clamp(p2.y + p2.vy, 40, H - 40);

        pk.x += pk.vx;
        pk.y += pk.vy;
        arenaForce(pk);
        pk.vx *= FRICTION;
        pk.vy *= FRICTION;
        pk.vx = clamp(pk.vx, -MAX_V, MAX_V);
        pk.vy = clamp(pk.vy, -MAX_V, MAX_V);
        reflectEdge(pk);
        pk.pushTrail();

        collide(p1);
        collide(p2);

        if (pk.x - pk.r <= 0 && Math.abs(pk.y - H * 0.5f) < GOAL_W * 0.5f) score("R");
        else if (pk.x + pk.r >= W && Math.abs(pk.y - H * 0.5f) < GOAL_W * 0.5f) score("L");

        long now = Time.ms();
        if (power == null && now >= nextPowerAt) spawnPower();
        if (power != null) {
            if (now - power.born > 8000) {
                power = null;
                nextPowerAt = now + 9000;
            } else if (circleHit(pk.x, pk.y, pk.r, power.x, power.y, power.r)) {
                Paddle pad = (pk.x < W / 2f) ? p1 : p2;
                applyPower(pad, power.type);
                power = null;
                nextPowerAt = now + (long) (9000 * 1.2);
            }
        }

        p1.tick(dt);
        p2.tick(dt);
    }

    private void score(String side) {
        if ("L".equals(side)) {
            scoreL += 1 + ("L".equals(lastGoalSide) ? 1 : 0);
            lastGoalSide = "L";
            resetPositions("right");
        } else {
            scoreR += 1 + ("R".equals(lastGoalSide) ? 1 : 0);
            lastGoalSide = "R";
            resetPositions("left");
        }
    }

    private void collide(Paddle pad)  {
        float dx = pk.x - pad.x;
        float dy = pk.y - pad.y;
        float dist = (float) Math.hypot(dx, dy);
        float minDist = pk.r + pad.r;

        if (dist < minDist && dist > 0) {
            // normal
            float nx = dx / dist;
            float ny = dy / dist;

            // empurra o puck para fora do bastão (sem "grudar")
            float overlap = minDist - dist;
            pk.x += nx * overlap;
            pk.y += ny * overlap;

            // calcula a velocidade resultante (impacto mais natural)
            float padSpeed = (float) Math.hypot(pad.vx, pad.vy);
            float impact = 1.1f + (pad.turbo ? 0.4f : 0f); // leve boost no turbo
            pk.vx = nx * padSpeed * impact;
            pk.vy = ny * padSpeed * impact;

            // se o bastão estiver parado, aplica leve repulsão
            if (padSpeed < 0.01f) {
                pk.vx += nx * 2f;
                pk.vy += ny * 2f;
            }

            // limita a velocidade máxima
            float spd = (float) Math.hypot(pk.vx, pk.vy);
            if (spd > MAX_V) {
                pk.vx = (pk.vx / spd) * MAX_V;
                pk.vy = (pk.vy / spd) * MAX_V;
            }
        }
    }


    private void spawnPower() {
        float px = W * 0.25f + rand.nextFloat() * W * 0.5f;
        float py = H * 0.2f + rand.nextFloat() * H * 0.6f;
        PowerUp.Type[] all = PowerUp.Type.values();
        PowerUp.Type t = all[rand.nextInt(all.length)];
        power = new PowerUp(t, px, py, 18);
    }

    private void applyPower(Paddle pad, PowerUp.Type type) {
        switch (type) {
            case TURBO -> pad.turbo = true;
            case SLOW -> pad.slow = true;
            case ENLARGE -> pad.r = Math.min(58, pad.r + 10);
            case SHRINK -> pad.r = Math.max(24, pad.r - 10);
        }
        new Thread(() -> {
            try { Thread.sleep(6000); } catch (InterruptedException ignored) {}
            if (type == PowerUp.Type.TURBO) pad.turbo = false;
            if (type == PowerUp.Type.SLOW) pad.slow = false;
        }).start();
    }

    private void resetPositions(String afterGoalSide) {
        p1.x = W * 0.15f; p1.y = H * 0.5f; p1.vx = p1.vy = 0;
        p2.x = W * 0.85f; p2.y = H * 0.5f; p2.vx = p2.vy = 0;
        kickoff("left".equals(afterGoalSide));
    }

    private void reflectEdge(Puck p) {
        if (p.x - p.r < 0) { p.x = p.r; p.vx = Math.abs(p.vx) * 0.9f; }
        if (p.x + p.r > W) { p.x = W - p.r; p.vx = -Math.abs(p.vx) * 0.9f; }
        if (p.y - p.r < 0) { p.y = p.r; p.vy = Math.abs(p.vy) * 0.9f; }
        if (p.y + p.r > H) { p.y = H - p.r; p.vy = -Math.abs(p.vy) * 0.9f; }
    }

    private void arenaForce(Puck p) {
        if (arena == ArenaType.CLASSIC) return;
        if (arena == ArenaType.BUMPS) {
            p.vy += Math.sin(p.x * 0.02f) * 0.03f;
        } else if (arena == ArenaType.WARP) {
            float cx = W * 0.5f, cy = H * 0.5f;
            float dx = (p.x - cx), dy = (p.y - cy);
            float d = (float) Math.hypot(dx, dy) + 0.001f;
            float s = -0.12f / d;
            p.vx += dx * s;
            p.vy += dy * s;
        }
    }

    private boolean circleHit(float x1, float y1, float r1, float x2, float y2, float r2) {
        return Math.hypot(x1 - x2, y1 - y2) <= (r1 + r2);
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private void drawArena() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.04f, 0.05f, 0.14f, 1f);
        sr.rect(0, 0, W, H);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(139 / 255f, 233 / 255f, 253 / 255f, 0.7f);
        sr.line(W / 2f, 0, W / 2f, H);
        sr.circle(W / 2f, H / 2f, 80);
        sr.end();
    }

    private void arc(ShapeRenderer s, float cx, float cy, float r, float startDeg, float degrees) {
        int seg = 50;
        float step = degrees / seg;
        for (int i = 0; i < seg; i++) {
            float a1 = (float) Math.toRadians(startDeg + i * step);
            float a2 = (float) Math.toRadians(startDeg + (i + 1) * step);
            float x1 = cx + (float) Math.cos(a1) * r, y1 = cy + (float) Math.sin(a1) * r;
            float x2 = cx + (float) Math.cos(a2) * r, y2 = cy + (float) Math.sin(a2) * r;
            s.line(x1, y1, x2, y2);
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        sr.dispose();
        if (font != null) font.dispose();
    }
}
