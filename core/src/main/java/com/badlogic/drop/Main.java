package com.badlogic.drop;


import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {


    Texture backgroundTexture;
    Texture rocketTexture;
    Texture asteroidTexture;
    Texture explosion;
    Sound crashSound;
    Sound explosionSound;
    Sound levelUp;
    Music music;
    Music music2;
    Music finalMusic;

    SpriteBatch spriteBatch;
    FitViewport viewport;

    Sprite explosionSprite;
    Sprite rocketSprite;

    Vector2 touchPos;

    Array<Sprite> asteroidSprites;

    float asteroidTimer;
    float timeElapsed;
    int score;
    BitmapFont font;
    GlyphLayout layout;
    int lives;
    boolean isGameOver;
    boolean gameStarted;
    int lastPlayedScore;


    Rectangle rocketRectangle;
    Rectangle asteroidRectangle;

    @Override
    public void create() {
        backgroundTexture = new Texture("background.png");
        rocketTexture = new Texture("spaceship.png");
        asteroidTexture = new Texture("asteroid.png");
        explosion =  new Texture("explosion.png");
        crashSound = Gdx.audio.newSound(Gdx.files.internal("crash.mp3"));
        explosionSound =  Gdx.audio.newSound(Gdx.files.internal("explosion.mp3"));
        levelUp =  Gdx.audio.newSound(Gdx.files.internal("level_up.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        music2 = Gdx.audio.newMusic(Gdx.files.internal("music2.mp3"));
        finalMusic = Gdx.audio.newMusic(Gdx.files.internal("final.mp3"));
        // Prepare your application here.

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        rocketSprite = new Sprite(rocketTexture);
        rocketSprite.setSize(1, 1);
        explosionSprite = new Sprite(explosion);
        rocketSprite.setPosition((viewport.getWorldWidth() - rocketSprite.getWidth()) / 2,0.1f);

        touchPos = new Vector2();

        asteroidSprites = new Array<>();

        rocketRectangle = new Rectangle();
        asteroidRectangle = new Rectangle();

        music.setLooping(true);
        music.setVolume(0.3f);
        music.play();

        font = new BitmapFont(); // default font included with LibGDX
        font.getData().setScale(0.04f); // makes the font small enough for your world unit.
        font.setColor(Color.WHITE); // optional styling
        font.setUseIntegerPositions(false);
        layout = new GlyphLayout(); // helps measure text width/height for alignment


        lives = 5;
        isGameOver = false;
        gameStarted = false;
        lastPlayedScore = 0;


    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        // Resize your application here. The parameters represent the new window size.
    }

    @Override
    public void render() {
        if (!gameStarted) {
            // Check if Enter is pressed to start the game
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                gameStarted = true; // Game starts when Enter is pressed
            }
            drawStartScreen(); // Draw the start screen
        } else {
            if (!isGameOver) {
                input();
                logic();
            } else {
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    resetGame();
                }
            }
            draw(); // Draw the game screen
        }
    }


    private void drawStartScreen() {
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        // Draw background and message for "Press Enter to Start"
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);

        // Draw the "Press Enter to Start" message
        layout.setText(font, "Press ENTER to Start");
        font.draw(spriteBatch, layout,
            (worldWidth - layout.width) / 2, worldHeight / 2);

        spriteBatch.end();
    }

    private void input() {
        float speed = 5f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            rocketSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            rocketSprite.translateX(-speed * delta);
        }

    }

    private void logic() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        float rocketWidth = rocketSprite.getWidth();
        float rocketHeight = rocketSprite.getHeight();



        rocketSprite.setX(MathUtils.clamp(rocketSprite.getX(), 0, worldWidth - rocketWidth));
        rocketSprite.setY(MathUtils.clamp(rocketSprite.getY(), 0,  worldHeight - rocketHeight));

        float delta = Gdx.graphics.getDeltaTime();

        rocketRectangle.set(rocketSprite.getX() + 0.2f, rocketSprite.getY() + 0.2f, rocketWidth - 0.4f, rocketHeight - 0.5f);

        int currentMilestone = (score / 100) * 100; // Get the nearest multiple of 100 below the current score
        if (currentMilestone > lastPlayedScore) {
            levelUp.play();
            lastPlayedScore = currentMilestone; // Update the last played milestone
        }

        for (int i = asteroidSprites.size - 1; i >= 0; i--) {
            Sprite asteroidSprite = asteroidSprites.get(i);
            float asteroidWidth = asteroidSprite.getWidth();
            float asteroidHeight = asteroidSprite.getHeight();

            if (score < 100) {
                asteroidSprite.translateY((-2f * delta));
                asteroidRectangle.set(asteroidSprite.getX(), asteroidSprite.getY(), asteroidWidth, asteroidHeight);
            } else if (score < 200) {
                asteroidSprite.translateY((-3f * delta));
                asteroidRectangle.set(asteroidSprite.getX(), asteroidSprite.getY(), asteroidWidth, asteroidHeight);
            }else if (score < 300) {
                asteroidSprite.translateY((-4f * delta));
                asteroidRectangle.set(asteroidSprite.getX(), asteroidSprite.getY(), asteroidWidth, asteroidHeight);
            } else if (score < 400) {
                asteroidSprite.translateY((-5f * delta));
                asteroidRectangle.set(asteroidSprite.getX(), asteroidSprite.getY(), asteroidWidth, asteroidHeight);
            } else if (score < 500) {
                asteroidSprite.translateY((-6f * delta));
                asteroidRectangle.set(asteroidSprite.getX(), asteroidSprite.getY(), asteroidWidth, asteroidHeight);
            } else if (score < 1000) {
                music.stop();
                music2.play();
                music2.setVolume(0.3f);
                asteroidSprite.translateY((-7f * delta));
                asteroidRectangle.set(asteroidSprite.getX(), asteroidSprite.getY(), asteroidWidth, asteroidHeight);
            } else {
                music2.stop();
                music.setVolume(0.3f);
                finalMusic.play();
                asteroidSprite.translateY((-8f * delta));
                asteroidRectangle.set(asteroidSprite.getX(), asteroidSprite.getY(), asteroidWidth, asteroidHeight);
            }


            if (asteroidSprite.getY() < -asteroidHeight) {
                asteroidSprites.removeIndex(i);
            } else if (rocketRectangle.overlaps(asteroidRectangle)) {
                asteroidSprites.removeIndex(i);
                crashSound.play();
                
                lives--;



                if (lives <= 0) {
                    isGameOver = true;
                    music.stop();
                    music2.stop();
                    finalMusic.stop();
                    crashSound.stop();
                    levelUp.stop();
                    explosionSound.play();
                    explosionSprite.setSize(1f, 1f);
                    explosionSprite.setPosition(rocketSprite.getX(), rocketSprite.getY());

                }
            }
        }
        asteroidTimer += delta;
        if (asteroidTimer > 0.75f && score < 500) {
            asteroidTimer = 0;
            createAsteroid();
        }
        if (score > 500 && asteroidTimer > 0.6f) {
            asteroidTimer = 0;
            createAsteroid();
        }

        if (!isGameOver) {
            timeElapsed += delta;
            score = (int)(timeElapsed * 10); // or tweak multiplier to scale difficulty
        }



    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        rocketSprite.draw(spriteBatch);

        for (Sprite asteroidSprite : asteroidSprites) {
            asteroidSprite.draw(spriteBatch);
        }

        // Draw lives
        layout.setText(font, "Lives: " + lives);
        font.draw(spriteBatch, layout, 6, viewport.getWorldHeight() - 0.2f);

// Draw score
        layout.setText(font, "Score: " + score);
        font.draw(spriteBatch, layout, 0.2f, viewport.getWorldHeight() - 0.2f);

// Draw Game Over if needed



        if (isGameOver) {
            layout.setText(font, "GAME OVER");
            font.draw(spriteBatch, layout,
                (viewport.getWorldWidth() - layout.width) / 2,
                viewport.getWorldHeight() / 2);

            explosionSprite.draw(spriteBatch);

        }


        spriteBatch.end();

    }

    private void createAsteroid() {
        float asteroidWidth = 0.75f;
        float asteroidHeight = 0.75f;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite asteroidSprite = new Sprite(asteroidTexture);
        asteroidSprite.setSize(asteroidWidth, asteroidHeight);
        asteroidSprite.setX(MathUtils.random(0f, worldWidth - asteroidWidth));
        asteroidSprite.setY(worldHeight);
        asteroidSprites.add(asteroidSprite);
    }

    private void resetGame() {
        // Reset all game variables to their initial values
        score = 0;
        lives = 5;
        isGameOver = false;
        gameStarted = false;
        asteroidSprites.clear();
        rocketSprite.setPosition((viewport.getWorldWidth() - rocketSprite.getWidth()) / 2,0.1f);
        timeElapsed = 0;
        music.play();
        lastPlayedScore = 0;
    }


    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        // Destroy application's resources here.
    }
}
