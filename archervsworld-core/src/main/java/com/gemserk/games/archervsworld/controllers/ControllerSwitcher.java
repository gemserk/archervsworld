package com.gemserk.games.archervsworld.controllers;

import java.util.ArrayList;

import com.badlogic.gdx.Input.Keys;
import com.gemserk.commons.gdx.controllers.Controller;
import com.gemserk.componentsengine.input.ButtonMonitor;
import com.gemserk.componentsengine.input.LibgdxButtonMonitor;

public class ControllerSwitcher implements Controller {

	ArrayList<BowController> controllers;

	int currentController = 0;

	ButtonMonitor switchButtonMonitor;
	
	public int getCurrentController() {
		return currentController;
	}

	public BowController getController() {
		return controllers.get(currentController);
	}
	
	public void setSwitchButtonMonitor(ButtonMonitor switchButtonMonitor) {
		this.switchButtonMonitor = switchButtonMonitor;
	}

	public ControllerSwitcher(ArrayList<BowController> controllers) {
		this(controllers, new LibgdxButtonMonitor(Keys.KEYCODE_TAB));
	}

	public ControllerSwitcher(ArrayList<BowController> controllers, ButtonMonitor buttonMonitor) {
		this.controllers = controllers;
		this.switchButtonMonitor = buttonMonitor;
	}
	
	protected boolean shouldSwitch() {
		switchButtonMonitor.update();
		return switchButtonMonitor.isPressed();
	}

	public void update(int delta) {
		
		if (!shouldSwitch())
			return;

		currentController++;
		if (currentController >= controllers.size())
			currentController = 0;
	}

}