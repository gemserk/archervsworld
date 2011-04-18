package com.gemserk.games.archervsworld.controllers;

import java.util.ArrayList;

import com.badlogic.gdx.Input.Keys;
import com.gemserk.componentsengine.input.ButtonMonitor;
import com.gemserk.componentsengine.input.LibgdxButtonMonitor;

public class ControllerSwitcher {

	ArrayList<BowController> controllers;

	int currentController = 0;

	public BowController getController() {
		return controllers.get(currentController);
	}

	public ControllerSwitcher(ArrayList<BowController> controllers) {
		this.controllers = controllers;
	}

	ButtonMonitor switchButtonMonitor = new LibgdxButtonMonitor(Keys.KEYCODE_TAB);

	protected boolean shouldSwitch() {
		switchButtonMonitor.update();
		return switchButtonMonitor.isPressed();
	}

	public void update() {
		
		if (!shouldSwitch())
			return;

		currentController++;
		if (currentController >= controllers.size())
			currentController = 0;
	}

}