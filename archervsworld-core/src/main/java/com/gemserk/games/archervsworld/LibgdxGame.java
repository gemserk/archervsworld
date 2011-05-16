package com.gemserk.games.archervsworld;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.animation4j.commons.values.converters.CommonConverters;
import com.gemserk.animation4j.converters.Converters;
import com.gemserk.animation4j.gdx.converters.LibgdxConverters;
import com.gemserk.commons.gdx.InternalScreen;
import com.gemserk.commons.gdx.ScreenAdapter;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.games.archervsworld.gamestates.GameData;
import com.gemserk.games.archervsworld.gamestates.PlayGameState;
import com.gemserk.games.archervsworld.gamestates.ScoreGameState;
import com.gemserk.games.archervsworld.gamestates.SplashGameState;

public class LibgdxGame extends Game {
	
	public InternalScreen gameScreen;
	
	public InternalScreen scoreScreen;

	private FadeTransitionScreen fadeTransitionScreen;

	@Override
	public void create() {
		Converters.register(Vector2.class, LibgdxConverters.vector2());
		Converters.register(Color.class, LibgdxConverters.color());
		Converters.register(FloatValue.class, CommonConverters.floatValue());
		
		GameData gameData = new GameData();
		gameData.gameOver = true;
		
		gameScreen = new InternalScreen(new PlayGameState(this, gameData));
		scoreScreen = new InternalScreen(new ScoreGameState(this, gameData));
		
		fadeTransitionScreen = new FadeTransitionScreen(this);
		
		InternalScreen splashScreen = new InternalScreen(new SplashGameState(this));
		
		setScreen(gameScreen);
		
		transition(null, splashScreen);
	}
	
	public void transition(ScreenAdapter nextScreen) {
		this.transition(nextScreen, false);
	}

	public void transition(ScreenAdapter nextScreen, boolean shouldDisposeCurrent) {
		fadeTransitionScreen.transition((ScreenAdapter) this.getScreen(), nextScreen, 1500, shouldDisposeCurrent);
		setScreen(fadeTransitionScreen);
	}

	public void transition(ScreenAdapter currentScreen, ScreenAdapter nextScreen) {
		fadeTransitionScreen.transition(currentScreen, nextScreen, 1500);
		setScreen(fadeTransitionScreen);
	}

}
