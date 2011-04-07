package com.gemserk.games.archervsworld;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.gemserk.commons.gdx.ScreenAdapter;

public class GameScreen extends ScreenAdapter {

	private final Game game;

	public GameScreen(Game game) {
		this.game = game;
	}

	@Override
	public void render(float delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void show() {

	}

	@Override
	public void dispose() {

	}

}