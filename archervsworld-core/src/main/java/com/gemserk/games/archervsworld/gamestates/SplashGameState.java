package com.gemserk.games.archervsworld.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.graphics.SpriteUtils;
import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
import com.gemserk.games.archervsworld.LibgdxGame;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class SplashGameState extends GameStateImpl {

	private final LibgdxGame game;

	private SpriteBatch spriteBatch;

	private ResourceManager<String> resourceManager;

	private Sprite gemserkLogoSprite;
	
	private int time;

	public SplashGameState(LibgdxGame game) {
		this.game = game;
	}

	@Override
	public void init() {
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		spriteBatch = new SpriteBatch();
		resourceManager = new ResourceManagerImpl<String>();

		new LibgdxResourceBuilder(resourceManager) {
			{
				setCacheWhenLoad(true);
				texture("GemserkLogoTexture", "data/images/logo-gemserk-512x128-white.png");
				sprite("GemserkLogoSprite", "GemserkLogoTexture");
			}
		};

		gemserkLogoSprite = resourceManager.getResourceValue("GemserkLogoSprite");
		
		SpriteUtils.resize(gemserkLogoSprite, width * 0.8f);
		SpriteUtils.centerOn(gemserkLogoSprite, width * 0.5f, height * 0.5f);
		
		time = 2000;
	}
	
	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		gemserkLogoSprite.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update(int delta) {
		time -= delta;
		if (time <= 0) 
			game.transition(game.gameScreen, true);
		
		if (Gdx.input.justTouched())
			time = 0;
	}

	@Override
	public void dispose() {
		resourceManager.unloadAll();
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
