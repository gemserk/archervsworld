package com.gemserk.games.archervsworld;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class DesktopApplication {
	public static void main (String[] argv) {
		new LwjglApplication(new LibgdxGame(), "Archer Vs Zombies", 800, 480, false);
	}
}
