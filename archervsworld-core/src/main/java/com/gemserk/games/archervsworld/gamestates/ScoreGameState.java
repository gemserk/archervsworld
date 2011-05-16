package com.gemserk.games.archervsworld.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
import com.gemserk.games.archervsworld.LibgdxGame;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class ScoreGameState extends GameStateImpl {

	private final LibgdxGame game;

	private SpriteBatch spriteBatch;

	private ResourceManager<String> resourceManager;

	private Sprite backgroundSprite;

	private TextButton tryAgainButton;

	private BitmapFont font;

	private GameData gameData;

	public ScoreGameState(LibgdxGame game, GameData gameData) {
		this.game = game;
		this.gameData = gameData;
	}

	@Override
	public void init() {
		spriteBatch = new SpriteBatch();
		resourceManager = new ResourceManagerImpl<String>();

		new LibgdxResourceBuilder(resourceManager) {
			{
				setCacheWhenLoad(true);
				texture("BackgroundTexture", "data/images/background-512x512.jpg", false);
				sprite("BackgroundSprite", "BackgroundTexture");
				font("Font", "data/fonts/font.png", "data/fonts/font.fnt");
			}
		};

		backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
		backgroundSprite.setPosition(0, 0);

		font = resourceManager.getResourceValue("Font");

		String buttonText = "Play again";

		tryAgainButton = new TextButton(font, buttonText, Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.4f);
	}

	@Override
	public void render(int delta) {
		if (spriteBatch == null)
			return;

		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();

		backgroundSprite.setPosition(0, 0);
		backgroundSprite.draw(spriteBatch);

		backgroundSprite.setPosition(backgroundSprite.getWidth(), 0);
		backgroundSprite.draw(spriteBatch);

		if (gameData.gameOver) {
			font.setColor(Color.RED);
			SpriteBatchUtils.drawCentered(spriteBatch, font, "Game Over", Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.7f);
			SpriteBatchUtils.drawCentered(spriteBatch, font, "Score: " + 150, Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.6f);
		}

		tryAgainButton.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update(int delta) {
		Synchronizers.synchronize();

		tryAgainButton.update();

		if (tryAgainButton.isReleased()) 
			game.transition(game.gameScreen, true);

	}

	@Override
	public void dispose() {
		resourceManager.unloadAll();
		spriteBatch.dispose();
		spriteBatch = null;
	}

	public void setGameData(GameData gameData) {
		this.gameData = gameData;
	}

}
