package com.gemserk.games.archervsworld;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class EditorApplication {
	public static void main (String[] argv) {
		new LwjglApplication(new GameEditor(), "Archer Vs Zombies", 800, 480, false);
	}
}
