package com.gemserk.games.archervsworld;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.gemserk.games.archervsworld.LibgdxGame;

public class DesktopApplication {
	public static void main (String[] argv) {
		new LwjglApplication(new LibgdxGame(), "Archer Vs World", 800, 480, false);
	}
}
