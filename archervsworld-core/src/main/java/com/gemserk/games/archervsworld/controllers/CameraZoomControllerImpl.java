package com.gemserk.games.archervsworld.controllers;

import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.controllers.CameraController;
import com.gemserk.componentsengine.input.ButtonMonitor;

public class CameraZoomControllerImpl implements CameraController {

	private final Camera camera;

	private final ButtonMonitor zoomInMonitor;

	private final ButtonMonitor zoomOutMonitor;

	@Override
	public Camera getCamera() {
		return camera;
	}

	public CameraZoomControllerImpl(Camera camera, ButtonMonitor zoomInMonitor, ButtonMonitor zoomOutMonitor) {
		this.camera = camera;
		this.zoomInMonitor = zoomInMonitor;
		this.zoomOutMonitor = zoomOutMonitor;
	}

	@Override
	public void update(int delta) {
		zoomInMonitor.update();
		zoomOutMonitor.update();

		if (zoomInMonitor.isHolded())
			camera.setZoom(camera.getZoom() + 0.05f * delta);

		if (zoomOutMonitor.isHolded())
			camera.setZoom(camera.getZoom() - 0.05f * delta);

	}

}