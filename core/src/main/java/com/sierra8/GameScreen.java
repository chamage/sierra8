package com.sierra8;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Random;

public class GameScreen implements Screen {

    final SierraGame game;
    private Random random;
    private boolean paused = false;

    private OrthographicCamera camera;
    private Player player;
    private EnemyManager enemyManager;

    private ShapeRenderer shape;
    private SpriteBatch batch;

    private BitmapFont font;


    public GameScreen(final SierraGame game){
        this.game = game;

        random = new Random();

        float viewportWidth = 1280;
        float viewportHeight = 720;

        camera = new OrthographicCamera(viewportWidth, viewportHeight);
        camera.position.set(viewportWidth / 2f, viewportHeight / 2f, 0);
        camera.update();

        shape = new ShapeRenderer();
        batch = new SpriteBatch();

        font = new BitmapFont();
        font.setUseIntegerPositions(false);
        font.getData().setScale(viewportHeight / Gdx.graphics.getHeight());

        player = new Player(viewportWidth / 2f, viewportHeight / 2f);
        enemyManager = new EnemyManager(1f, 30, 220f);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            if (paused) resume();
            else pause();
        }

        if (!paused) {
            player.update(delta, camera);
            enemyManager.update(delta, player, player.getBullets());

            enemyManager.setPlayerDeathListener(new PlayerDeathListener() {
                @Override
                public void onPlayerDeath() {
                    game.setScreen(new PlayerDeathScreen(game));
                }
            });

            camera.position.set(player.getPosition().x, player.getPosition().y, 0);
            camera.update();
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shape.setProjectionMatrix(camera.combined);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        createBackground();
        shape.end();

        player.render(shape, camera);
        enemyManager.render(shape);

        if (paused){
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0, 0, 0, 0.7f);
            shape.rect(camera.position.x - camera.viewportWidth / 2,
                camera.position.y - camera.viewportHeight / 2,
                camera.viewportWidth, camera.viewportHeight);
            shape.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        String debug = "X: " + player.getPosition().x + " Y: " + player.getPosition().y
            + " Bullets: " + player.getBullets().size() + " Enemies: " + enemyManager.getEnemies().size();
        font.setColor(Color.WHITE);
        font.draw(batch, debug, camera.position.x - camera.viewportWidth / 2 + 5,
            camera.position.y + camera.viewportHeight / 2 - 10);

        if (paused) {
            font.getData().setScale(2f);
            font.setColor(Color.WHITE);
            font.draw(batch, "PAUSED", camera.position.x - 50, camera.position.y + 10);
            font.getData().setScale(1f); // Reset scale
        }

        batch.end();

    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.position.set(width / 2f, height / 2f, 0);
        camera.update();
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        shape.dispose();
        batch.dispose();
    }

    private void createBackground(){

        int squareSize = 60;

        float camLeft   = camera.position.x - camera.viewportWidth / 2;
        float camRight  = camera.position.x + camera.viewportWidth / 2;
        float camBottom = camera.position.y - camera.viewportHeight / 2;
        float camTop    = camera.position.y + camera.viewportHeight / 2;

        int startX = (int)(camLeft / squareSize) - 1;
        int endX   = (int)(camRight / squareSize) + 1;
        int startY = (int)(camBottom / squareSize) - 1;
        int endY   = (int)(camTop / squareSize) + 1;

        for (int i = startX; i <= endX; i++) {
            for (int j = startY; j <= endY; j++) {
                if ((i + j) % 2 == 0) {
                    shape.setColor(Color.DARK_GRAY);
                } else {
                    shape.setColor(Color.LIGHT_GRAY);
                }
                shape.rect(i * squareSize, j * squareSize, squareSize, squareSize);
            }
        }
    }
}
