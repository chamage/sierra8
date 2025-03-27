package com.sierra8;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameScreen implements Screen {

    // Core game ref
    private final SierraGame game;

    // Utilities
    private final Random random = new Random();
    private boolean paused = false;

    // Camera and render
    private OrthographicCamera camera;
    private ShapeRenderer shape;
    private SpriteBatch batch;

    // Assets
    private Sound shootSound;
    private Music[] musicTracks;
    private int currentTrackIndex = 0;


    // Game components
    private Player player;
    private EnemyManager enemyManager;

    private Texture grassTexture;



    public GameScreen(final SierraGame game){
        this.game = game;

        intitialize();
        loadAssets();
    }

    private void intitialize(){

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        shape = new ShapeRenderer();
        batch = new SpriteBatch();

        player = new Player(0, 0);
        enemyManager = new EnemyManager(1f, 30, 220f);

        enemyManager.setPlayerDeathListener(new PlayerDeathListener() {
            @Override
            public void onPlayerDeath() {
                stopTrack();
                game.setScreen(new PlayerDeathScreen(game));
            }
        });

        enemyManager.setEnemyDeathListener(new EnemyDeathListener() {
            @Override
            public void onEnemyDeath() {
                player.enemyKilled();
            }
        });

        player.setPistolShootListener(new PistolShootListener() {
            @Override
            public void onPistolShot() {
                shootSound.play(game.soundVolume);
            }
        });
    }

    private void loadAssets(){
        musicTracks = new Music[8];
        for (int i = 0; i < musicTracks.length; i++){
            musicTracks[i] = Gdx.audio.newMusic(Gdx.files.internal("music/loop" + (i+1) + ".mp3"));
        }

        shootSound = Gdx.audio.newSound(Gdx.files.internal("sound/shoot.mp3"));

        grassTexture = new Texture(Gdx.files.internal("textures/grass.png"));

        playTrack();
    }

    @Override
    public void show() {
    }



    @Override
    public void render(float delta) {
        clearScreen();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            if (paused) resume();
            else pause();
        }

        renderGame();

        if (!paused) {
            update(delta);
        }
        else{
            noUpdate();
        }
    }

    private void update(float delta){
        player.update(delta, camera);
        enemyManager.update(delta, player, player.getBullets());
        camera.position.set(player.getPosition().x, player.getPosition().y, 0);
        camera.update();
    }

    private void noUpdate(){
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0, 0, 0, 0.7f);
        shape.rect(camera.position.x - camera.viewportWidth / 2,
            camera.position.y - camera.viewportHeight / 2,
            camera.viewportWidth, camera.viewportHeight);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        game.fontMain.draw(batch, "PAUSED", camera.position.x - 50, camera.position.y + 10);
        batch.end();
    }

    private void renderGame(){
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        createBackground();
        player.render(shape, camera);
        enemyManager.render(shape);
        shape.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        String debug = "X: " + player.getPosition().x + " Y: " + player.getPosition().y
            + " Bullets: " + player.getBullets().size() + " Enemies: " + enemyManager.getEnemies().size() + " FPS: " + Gdx.graphics.getFramesPerSecond();
        game.fontSmaller.draw(batch, debug, camera.position.x - camera.viewportWidth / 2 + 5,
            camera.position.y + camera.viewportHeight / 2 - 10);
        String killStreak = "Enemies killed: " + player.getEnemiesKilled();
        game.fontSmaller.draw(batch, killStreak, player.getPosition().x + 30, player.getPosition().y + 30);
        String ammoDisplay = player.getCurrentMag() + " / 10";
        game.fontSmaller.draw(batch, ammoDisplay, player.getPosition().x - 30, player.getPosition().y - 30);
        batch.end();
    }

    private void clearScreen(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
        stopTrack();
    }

    @Override
    public void dispose() {
        shape.dispose();
        batch.dispose();
        stopTrack();
        for (Music music : musicTracks){
            music.dispose();
        }
    }

    private void createBackground() {
        float textureWidth = grassTexture.getWidth();
        float textureHeight = grassTexture.getHeight();

        float camX = camera.position.x - camera.viewportWidth / 2f;
        float camY = camera.position.y - camera.viewportHeight / 2f;

        float offsetX = camX % textureWidth;
        float offsetY = camY % textureHeight;

        if (offsetX < 0) offsetX += textureWidth;
        if (offsetY < 0) offsetY += textureHeight;

        batch.begin();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                batch.draw(
                    grassTexture,
                    camX - offsetX + x * textureWidth,
                    camY - offsetY + y * textureHeight
                );
            }
        }
        batch.end();
    }

    private void createBackground2(){

        int squareSize = 100;

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

    private void saveGameData(){
        Vector2 playerPosition = player.getPosition();
        List<Vector2> enemyPositions = new ArrayList<>();
        for (Enemy enemy : enemyManager.getEnemies()) {
            enemyPositions.add(enemy.getPosition());
        }
        List<Vector2> bulletPositions = new ArrayList<>();
        for (Bullet bullet : player.getBullets()) {
            bulletPositions.add(bullet.getPosition());
        }

        GameData gameData = new GameData(playerPosition, enemyPositions, bulletPositions);
    }

    private void playTrack(){
        currentTrackIndex = random.nextInt(musicTracks.length);
        musicTracks[currentTrackIndex].setLooping(false);
        musicTracks[currentTrackIndex].setVolume(game.musicVolume);
        musicTracks[currentTrackIndex].play();

        musicTracks[currentTrackIndex].setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                playTrack();
            }
        });
    }

    private void stopTrack(){
        for (Music music : musicTracks){
            if (music.isPlaying()){
                music.stop();
            }
        }
    }
}
