package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.controllers.CameraController;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.commons.gdx.math.MathUtils2;

public class CameraControllerLibgdxPointerImpl implements CameraController {

	private final Vector2 previousPosition = new Vector2();

	private final LibgdxPointer libgdxPointer;

	private final Vector2 tmp = new Vector2();

	private final Rectangle area;

	private boolean inside = true;

	private Camera camera;

	private int timeSamePosition = 0;

	private int timeToZoom = 1000;

	private boolean zooming = false;

	private float currentZoom;

	private float maxZoom;

	private float minZoom;

	@Override
	public Camera getCamera() {
		return camera;
	}

	public CameraControllerLibgdxPointerImpl(Camera camera, LibgdxPointer libgdxPointer, Rectangle area) {
		this.camera = camera;
		this.area = area;
		this.libgdxPointer = libgdxPointer;
		this.maxZoom = camera.getZoom() * 1.5f;
		this.minZoom = camera.getZoom() * 0.5f;
	}

	@Override
	public void update(int delta) {

		if (libgdxPointer.wasReleased)
			zooming = false;

		if (libgdxPointer.wasPressed) {
			previousPosition.set(libgdxPointer.getPressedPosition());
			inside = MathUtils2.inside(area, previousPosition);
			currentZoom = camera.getZoom();
			return;
		}

		if (!libgdxPointer.touched || !inside)
			return;

		Vector2 pointerPosition = libgdxPointer.getPosition();

		tmp.set(previousPosition);
		tmp.sub(pointerPosition);
		tmp.mul(1f / camera.getZoom());

		if (tmp.len() <= 0f) {
			timeSamePosition += delta;
		} else {
			timeSamePosition = 0;
		}

		if (timeSamePosition > timeToZoom)
			zooming = true;

		if (zooming) {

			tmp.set(previousPosition);
			tmp.sub(pointerPosition);

			camera.setZoom(currentZoom - tmp.x * 0.1f);
			// if (camera.getZoom() > maxZoom)
			// camera.setZoom(maxZoom);
			camera.setZoom(MathUtils2.truncate(camera.getZoom(), minZoom, maxZoom));
		} else {
			camera.setPosition(camera.getX() + tmp.x, camera.getY() + tmp.y);
			previousPosition.set(pointerPosition);
		}

		// System.out.println(MessageFormat.format("position=({0},{1}), zoom={2}, angle={3}", camera.getX(), camera.getY(), camera.getZoom(), camera.getAngle()));

	}

}