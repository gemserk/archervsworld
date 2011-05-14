package com.gemserk.games.archervsworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.Transition;
import com.gemserk.animation4j.transitions.Transitions;
import com.gemserk.animation4j.transitions.event.TransitionEventHandler;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.ScreenAdapter;
import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class FadeTransitionScreen extends ScreenAdapter {

	private final LibgdxGame game;

	private final Color fadeColor = new Color();

	private final Color startColor = new Color(0f, 0f, 0f, 1f);

	private final Color endColor = new Color(0f, 0f, 0f, 0f);

	private ResourceManager<String> resourceManager;

	private SpriteBatch spriteBatch;

	private Sprite overlay;

	private ScreenAdapter currentScreen;

	private ScreenAdapter nextScreen;

	private int time;

	private boolean shouldDisposeCurrent;

	public void transition(ScreenAdapter currentScreen, ScreenAdapter nextScreen, int time) {
		this.transition(currentScreen, nextScreen, time, false);
	}
	
	public void transition(ScreenAdapter currentScreen, ScreenAdapter nextScreen, int time, boolean shouldDisposeCurrent) {
		this.currentScreen = currentScreen;
		this.nextScreen = nextScreen;
		this.time = time;
		this.shouldDisposeCurrent = shouldDisposeCurrent;
	}

	public FadeTransitionScreen(LibgdxGame game) {
		this.game = game;
		spriteBatch = new SpriteBatch();
		resourceManager = new ResourceManagerImpl<String>();
		new LibgdxResourceBuilder(resourceManager) {
			{
				texture("OverlayTexture", "data/images/white-rectangle.png");
				sprite("OverlaySprite", "OverlayTexture");
			}
		};
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void show() {
		overlay = resourceManager.getResourceValue("OverlaySprite");
		overlay.setPosition(0, 0);
		overlay.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (currentScreen == null) {
			currentScreen = nextScreen;
			Synchronizers.transition(fadeColor, Transitions.transitionBuilder(startColor).end(endColor).time(time / 2), new TransitionEventHandler() {
				@Override
				public void onTransitionFinished(Transition transition) {
					game.setScreen(nextScreen);
				}
			});
		} else {
			Synchronizers.transition(fadeColor, Transitions.transitionBuilder(endColor).end(startColor).time(time / 2), new TransitionEventHandler() {
				@Override
				public void onTransitionFinished(Transition transition) {
					if (shouldDisposeCurrent)
						currentScreen.dispose();
					currentScreen = nextScreen;
					Synchronizers.transition(fadeColor, Transitions.transitionBuilder(startColor).end(endColor).time(time / 2), new TransitionEventHandler() {
						@Override
						public void onTransitionFinished(Transition transition) {
							game.setScreen(nextScreen);
						}
					});
				}
			});
		}
	}

	@Override
	public void internalRender(float delta) {
		if (currentScreen != null)
			currentScreen.internalRender(delta);

		if (spriteBatch == null)
			return;

		spriteBatch.begin();
		overlay.setColor(fadeColor);
		overlay.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void internalUpdate(float delta) {
		long deltaInMs = (long) (delta * 1000f);
		Synchronizers.synchronize(deltaInMs);
	}

	@Override
	public void dispose() {
		resourceManager.unloadAll();
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
