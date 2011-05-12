package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.animation4j.transitions.Transitions;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.controllers.CameraController;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.commons.values.ValueBuilder;
import com.gemserk.componentsengine.input.ButtonMonitor;

public class CameraControllerButtonMonitorImpl implements CameraController {

	private final Vector2 position = new Vector2();

	private final FloatValue zoom = new FloatValue(1f);

	private final ButtonMonitor left;

	private final ButtonMonitor right;

	private final ButtonMonitor up;

	private final ButtonMonitor down;

	private final ButtonMonitor zoomIn;

	private final ButtonMonitor zoomOut;

	private final Camera cameraImpl;

	public Vector2 getPosition() {
		return position;
	}

	public float getZoom() {
		return zoom.value;
	}

	@Override
	public Camera getCamera() {
		return cameraImpl;
	}

	public CameraControllerButtonMonitorImpl(Camera cameraImpl, //
			ButtonMonitor left, ButtonMonitor right, //
			ButtonMonitor up, ButtonMonitor down, //
			ButtonMonitor zoomIn, ButtonMonitor zoomOut) {
		this.left = left;
		this.right = right;
		this.up = up;
		this.down = down;
		this.zoomIn = zoomIn;
		this.zoomOut = zoomOut;
		this.cameraImpl = cameraImpl;
		this.position.set(cameraImpl.getX(), cameraImpl.getY());
		this.zoom.value = cameraImpl.getZoom();
	}

	@Override
	public void update(int delta) {

		if (down.isHolded())
			position.y -= 0.01f * delta;

		if (up.isHolded())
			position.y += 0.01f * delta;

		if (right.isHolded())
			position.x += 0.01f * delta;

		if (left.isHolded())
			position.x -= 0.01f * delta;

		if (zoomIn.isPressed()) {
			Synchronizers.transition(zoom, Transitions.transitionBuilder(zoom).end(ValueBuilder.floatValue(zoom.value * 2f)).time(300).build());
		}

		if (zoomOut.isPressed()) {
			Synchronizers.transition(zoom, Transitions.transitionBuilder(zoom).end(ValueBuilder.floatValue(zoom.value * 0.5f)).time(300).build());
		}

		cameraImpl.setPosition(position.x, position.y);
		cameraImpl.setZoom(zoom.value);
	}

}