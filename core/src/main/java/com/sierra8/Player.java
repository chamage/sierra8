package com.sierra8;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;

public class Player {

    private Vector2 position;
    private float speed;
    private float speedSprint;
    private float rotation;
    private float size = 30f;
    private ArrayList<Bullet> bullets;
    private Circle hitbox;
    private float shootCooldown;
    private float shootTimer;
    private Sound shootSound;
    private int enemiesKilled;
    private int magSize, currentMag;
    private float reloadSpeed = 3f;
    private PistolShootListener pistolShootListener;

    public Player(float x, float y){
        this.position = new Vector2(x, y);
        this.speed = 300f;
        this.speedSprint = 1.5f;
        this.bullets = new ArrayList<>();
        this.hitbox = new Circle(position, size*.6f);
        this.shootCooldown = 1f;
        this.shootTimer = 1f;
        this.enemiesKilled = 0;
        this.magSize = 10;
        this.currentMag = 10;
    }

    public void setPistolShootListener(PistolShootListener pistolShootListener) {
        this.pistolShootListener = pistolShootListener;
    }

    public void update(float delta, Camera camera){

        float currentSpeed = speed;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            currentSpeed *= speedSprint;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)){
            position.y += currentSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)){
            position.y -= currentSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)){
            position.x += currentSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)){
            position.x -= currentSpeed * delta;
        }

        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        Vector2 direction = new Vector2(mousePos.x - position.x, mousePos.y - position.y);
        rotation = direction.angleDeg();

        hitbox.setPosition(position);

        shootTimer += 0.1f;
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT ) && shootTimer >= shootCooldown){
            if (currentMag > 0){
                currentMag--;
                shootBullet();
                shootTimer = 0f;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)){
            currentMag = magSize;
        }

        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update(delta);
            if (bullet.isOutOfRange()) {
                bullets.remove(i);
            }
        }
    }

    public void render(ShapeRenderer shape, Camera camera){
        shape.begin(ShapeRenderer.ShapeType.Filled);

        float tipX   = position.x + MathUtils.cosDeg(rotation) * size;
        float tipY   = position.y + MathUtils.sinDeg(rotation) * size;
        float leftX  = position.x + MathUtils.cosDeg(rotation + 135) * size;
        float leftY  = position.y + MathUtils.sinDeg(rotation + 135) * size;
        float rightX = position.x + MathUtils.cosDeg(rotation - 135) * size;
        float rightY = position.y + MathUtils.sinDeg(rotation - 135) * size;

        shape.setColor(Color.BLUE);
        shape.triangle(tipX, tipY, leftX, leftY, rightX, rightY);

        shape.setColor(Color.GREEN);
        for (Bullet bullet : bullets){
            bullet.render(shape);
        }

        shape.end();

    }

    public Vector2 getPosition(){
        return position;
    }

    public ArrayList<Bullet> getBullets(){
        return bullets;
    }

    private void shootBullet(){
        float size = 30f;
        float bulletX = position.x + MathUtils.cosDeg(rotation) * size;
        float bulletY = position.y + MathUtils.sinDeg(rotation) * size;

        float bulletSpeed = 500f;

        Vector2 bulletDirection = new Vector2(MathUtils.cosDeg(rotation), MathUtils.sinDeg(rotation)).nor();
        bullets.add(new Bullet(new Vector2(bulletX, bulletY), bulletDirection, bulletSpeed));
        pistolShootListener.onPistolShot();
    }

    public Circle getHitbox(){
        return hitbox;
    }

    public int getEnemiesKilled(){
        return enemiesKilled;
    }
    public void enemyKilled(){
        enemiesKilled++;
    }
    public int getCurrentMag(){return currentMag; }

}
