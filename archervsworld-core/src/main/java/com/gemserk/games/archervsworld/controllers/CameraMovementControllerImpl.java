package com.gemserk.games.archervsworld.controllers;

import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.controllers.CameraController;
import com.gemserk.componentsengine.input.AnalogInputMonitor;
import com.gemserk.componentsengine.input.ButtonMonitor;

public class CameraMovementControllerImpl implements CameraController {

	private final AnalogInputMonitor xMonitor;

	private final AnalogInputMonitor yMonitor;

	private final Camera camera;

	private final ButtonMonitor moveMonitor;

	@Override
	public Camera getCamera() {
		return camera;
	}

	public CameraMovementControllerImpl(Camera camera, AnalogInputMonitor xMonitor, AnalogInputMonitor yMonitor, ButtonMonitor moveMonitor) {
		this.camera = camera;
		this.xMonitor = xMonitor;
		this.yMonitor = yMonitor;
		this.moveMonitor = moveMonitor;
	}

	@Override
	public void update(int delta) {
		float diffx = 0f;
		float diffy = 0f;

		moveMonitor.update();
		xMonitor.update();
		yMonitor.update();
		
		if (!moveMonitor.isHolded())
			return;

		if (xMonitor.hasChanged())
			diffx = xMonitor.getValue() - xMonitor.getOldValue();

		if (yMonitor.hasChanged())
			diffy = yMonitor.getValue() - yMonitor.getOldValue();

		if (diffx == 0 && diffy == 0)
			return;

		diffx *= 1f / camera.getZoom();
		diffy *= 1f / camera.getZoom();

		camera.setPosition(camera.getX() - diffx, camera.getY() + diffy);
	}

}